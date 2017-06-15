package com.lad.controller;

import com.lad.bo.ChatroomBo;
import com.lad.bo.IMTermBo;
import com.lad.bo.UserBo;
import com.lad.service.IChatroomService;
import com.lad.service.IIMTermService;
import com.lad.service.IUserService;
import com.lad.util.CommonUtil;
import com.lad.util.Constant;
import com.lad.util.ERRORCODE;
import com.lad.util.IMUtil;
import com.lad.vo.ChatroomVo;
import com.lad.vo.UserVo;
import com.pushd.ImAssistant;
import com.pushd.Message;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Controller
@RequestMapping("chatroom")
public class ChatroomController extends BaseContorller {

	@Autowired
	private IChatroomService chatroomService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IIMTermService iMTermService;

	@RequestMapping("/create")
	@ResponseBody
	public String create(String name, HttpServletRequest request,
			HttpServletResponse response) {
		HttpSession session = request.getSession();
		if (session.isNew()) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		if (session.getAttribute("isLogin") == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		UserBo userBo = (UserBo) session.getAttribute("userBo");
		if (userBo == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		userBo = userService.getUser(userBo.getId());
		if (StringUtils.isEmpty(name)) {
			return CommonUtil.toErrorResult(
					ERRORCODE.CHATROOM_NAME_NULL.getIndex(),
					ERRORCODE.CHATROOM_NAME_NULL.getReason());
		}
		ChatroomBo chatroomBo = new ChatroomBo();
		chatroomBo.setName(name);
		chatroomBo.setType(2);
		chatroomBo.setCreateuid(userBo.getId());
		chatroomService.insert(chatroomBo);
		HashSet<String> chatrooms = userBo.getChatrooms();
		chatrooms.add(chatroomBo.getId());
		userBo.setChatrooms(chatrooms);
		userService.updateChatrooms(userBo);
		ImAssistant assistent = ImAssistant.init("180.76.138.200", 2222);
		if (assistent == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.PUSHED_CONNECT_ERROR.getIndex(),
					ERRORCODE.PUSHED_CONNECT_ERROR.getReason());
		}
		String term = "";
		IMTermBo iMTermBo = iMTermService.selectByUserid(userBo.getId());
		if (iMTermBo == null) {
			term = IMUtil.getTerm(assistent);
			updateIMTerm(userBo.getId(), term);
		} else {
			term = iMTermBo.getTerm();
		}
		assistent.setServerTerm(term);
		try {
			Message message3 = assistent.subscribe(name, chatroomBo.getId(),
					userBo.getId());
			if (message3.getStatus() == Message.Status.termError) {
				term =  IMUtil.getTerm(assistent);
				iMTermService.updateByUserid(userBo.getId(), term);
				assistent.setServerTerm(term);
				Message message4 = assistent.subscribe(name, chatroomBo.getId(),
						userBo.getId());
				if (Message.Status.success != message4.getStatus()) {
					return CommonUtil.toErrorResult(message4.getStatus(),
							message4.getMsg());
				}
			} else if (Message.Status.success != message3.getStatus()) {
				return CommonUtil.toErrorResult(message3.getStatus(),
						message3.getMsg());
			}
		} finally {
			assistent.close();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("channelId", chatroomBo.getId());
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/update-name")
	@ResponseBody
	public String updateName(String roomid, String name,
			HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		if (session.isNew()) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		if (session.getAttribute("isLogin") == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		UserBo userBo = (UserBo) session.getAttribute("userBo");
		if (userBo == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		userBo = userService.getUser(userBo.getId());
		if (StringUtils.isEmpty(name)) {
			return CommonUtil.toErrorResult(
					ERRORCODE.CHATROOM_NAME_NULL.getIndex(),
					ERRORCODE.CHATROOM_NAME_NULL.getReason());
		}
		if (StringUtils.isEmpty(roomid)) {
			return CommonUtil.toErrorResult(
					ERRORCODE.CHATROOM_ID_NULL.getIndex(),
					ERRORCODE.CHATROOM_ID_NULL.getReason());
		}
		ChatroomBo chatroomBo = chatroomService.get(roomid);
		if (null == chatroomBo) {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		chatroomBo.setName(name);
		chatroomService.updateName(chatroomBo);
		return Constant.COM_RESP;
	}

	@RequestMapping("/insert-user")
	@ResponseBody
	public String insertUser(String userids, String chatroomid,
							 HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		if (session.isNew()) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		if (session.getAttribute("isLogin") == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		UserBo userBo = (UserBo) session.getAttribute("userBo");
		if (userBo == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		userBo = userService.getUser(userBo.getId());
		if (StringUtils.isEmpty(userids)) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_ID.getIndex(),
					ERRORCODE.ACCOUNT_ID.getReason());
		}
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (null == chatroomBo) {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		//判断参数是一个还是多个
		String[] useridArr;
		if (userids.indexOf(',') > 0) {
			useridArr = userids.trim().split(",");
		} else {
			useridArr = new String[]{userids};
		}
		IMTermBo imTermBo = iMTermService.selectByUserid(userBo.getId());
		String term = "";
		if (imTermBo != null) {
			term = imTermBo.getTerm();
		}
		//第一个为返回结果信息，第二位term信息
		String[] result = IMUtil.subscribe("", chatroomid, term, useridArr);
		if (!result[0].equals(IMUtil.FINISH)) {
			return result[0];
		}
		updateIMTerm(userBo.getId(), result[1]);
		HashSet<String> set = chatroomBo.getUsers();
		for (String userid : useridArr) {
			updateIMTerm(userid, result[1]);
			UserBo user = userService.getUser(userid);
			if (null == user) {
				return CommonUtil.toErrorResult(ERRORCODE.USER_NULL.getIndex(),
						 ERRORCODE.USER_NULL.getReason() + ", "+ userid);
			}
			HashSet<String> chatroom = user.getChatrooms();
			//个人聊天室中没有当前聊天室，则添加到个人的聊天室
			if (!chatroom.contains(chatroomBo.getId())) {
				chatroom.add(chatroomBo.getId());
				user.setChatrooms(chatroom);
				userService.updateChatrooms(user);
			}
			set.add(userid);
		}
		chatroomBo.setUsers(set);
		chatroomService.updateUsers(chatroomBo);
		return Constant.COM_RESP;
	}
	/**
	 *
	 * @param userid
	 * @param term
	 */
	private void updateIMTerm(String userid, String term){
		IMTermBo imTermBo = iMTermService.selectByUserid(userid);
		if (imTermBo == null) {
			imTermBo = new IMTermBo();
			imTermBo.setTerm(term);
			imTermBo.setUserid(userid);
			iMTermService.insert(imTermBo);
		} else {
			iMTermService.updateByUserid(userid, term);
		}
	}

	@RequestMapping("/delete-user")
	@ResponseBody
	public String deltetUser(String userids, String chatroomid,
							 HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		if (session.isNew()) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		if (session.getAttribute("isLogin") == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		UserBo userBo = (UserBo) session.getAttribute("userBo");
		if (userBo == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		userBo = userService.getUser(userBo.getId());
		if (StringUtils.isEmpty(userids)) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_ID.getIndex(),
					ERRORCODE.ACCOUNT_ID.getReason());
		}
		String[] useridArr;
		if (userids.indexOf(',') > 0) {
			useridArr = userids.trim().split(",");
		} else {
			useridArr = new String[]{userids.trim()};
		}
		IMTermBo imTermBo = iMTermService.selectByUserid(userBo.getId());
		String term = "";
		if (imTermBo != null) {
			term = imTermBo.getTerm();
		}
		//第一个为返回结果信息，第二位term信息
		String[] result = IMUtil.unSubscribe(chatroomid, term, useridArr);
		if (!result[0].equals(IMUtil.FINISH)) {
			return result[0];
		}
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (null == chatroomBo) {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		HashSet<String> set = chatroomBo.getUsers();
		for (String userid : useridArr) {
			updateIMTerm(userid, result[1]);
			UserBo user = userService.getUser(userid);
			if (null == user) {
				return CommonUtil.toErrorResult(ERRORCODE.USER_NULL.getIndex(),
						ERRORCODE.USER_NULL.getReason() + ", "+ userid);
			}
			HashSet<String> chatroom = user.getChatrooms();
			chatroom.remove(chatroomBo.getId());
			user.setChatrooms(chatroom);
			userService.updateChatrooms(user);
			set.remove(userid);
		}
		chatroomBo.setUsers(set);
		chatroomService.updateUsers(chatroomBo);
		return Constant.COM_RESP;
	}

	@RequestMapping("/get-friends")
	@ResponseBody
	public String getFriends(HttpServletRequest request,
			HttpServletResponse response) throws IllegalAccessException,
			InvocationTargetException {
		HttpSession session = request.getSession();
		if (session.isNew()) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		if (session.getAttribute("isLogin") == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		UserBo userBo = (UserBo) session.getAttribute("userBo");
		if (userBo == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		userBo = userService.getUser(userBo.getId());
		List<String> friends = userBo.getFriends();
		List<UserVo> userList = new LinkedList<UserVo>();
		for (String id : friends) {
			UserBo temp = userService.getUser(id);
			if (null != temp) {
				UserVo vo = new UserVo();
				BeanUtils.copyProperties(vo, temp);
				userList.add(vo);
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("friends", userList);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/get-my-chatrooms")
	@ResponseBody
	public String getChatrooms(HttpServletRequest request,
			HttpServletResponse response) throws IllegalAccessException,
			InvocationTargetException {
		HttpSession session = request.getSession();
		if (session.isNew()) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		if (session.getAttribute("isLogin") == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		UserBo userBoTemp = (UserBo) session.getAttribute("userBo");
		if (userBoTemp == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		UserBo userBo = userService.getUser(userBoTemp.getId());
		HashSet<String> Chatrooms = userBo.getChatrooms();
		HashSet<String> ChatroomsTop = userBo.getChatroomsTop();
		List<ChatroomVo> ChatroomList = new LinkedList<ChatroomVo>();
		for (String id : ChatroomsTop) {
			ChatroomBo temp = chatroomService.get(id);
			if (null != temp) {
				ChatroomVo vo = new ChatroomVo();
				BeanUtils.copyProperties(vo, temp);
				vo.setTop(1);
				ChatroomList.add(vo);
			}
		}
		for (String id : Chatrooms) {
			ChatroomBo temp = chatroomService.get(id);
			if (null != temp) {
				ChatroomVo vo = new ChatroomVo();
				BeanUtils.copyProperties(vo, temp);
				ChatroomList.add(vo);
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("ChatroomList", ChatroomList);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/get-chatroom-info")
	@ResponseBody
	public String getChatroomInfo(
			@RequestParam(required = true) String chatroomid,
			HttpServletRequest request, HttpServletResponse response)
			throws IllegalAccessException, InvocationTargetException {
		HttpSession session = request.getSession();
		if (session.isNew()) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		if (session.getAttribute("isLogin") == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		UserBo userBo = (UserBo) session.getAttribute("userBo");
		if (userBo == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		userBo = userService.getUser(userBo.getId());
		ChatroomBo temp = chatroomService.get(chatroomid);
		ChatroomVo vo = new ChatroomVo();
		if (null != temp) {
			BeanUtils.copyProperties(vo, temp);
		} else {
			return CommonUtil.toErrorResult(
					ERRORCODE.CHATROOM_ID_NULL.getIndex(),
					ERRORCODE.CHATROOM_ID_NULL.getReason());
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("chatroom", vo);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/set-top")
	@ResponseBody
	public String setTop(String chatroomid, HttpServletRequest request,
			HttpServletResponse response) {
		HttpSession session = request.getSession();
		if (session.isNew()) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		if (session.getAttribute("isLogin") == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		UserBo userBo = (UserBo) session.getAttribute("userBo");
		if (userBo == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		userBo = userService.getUser(userBo.getId());
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (null == chatroomBo) {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		HashSet<String> chatrooms = userBo.getChatrooms();
		LinkedHashSet<String> chatroomsTop = userBo.getChatroomsTop();
		if (chatrooms.contains(chatroomid)) {
			chatrooms.remove(chatroomid);
		} else {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		if (chatroomsTop.contains(chatroomid)) {
			return CommonUtil.toErrorResult(
					ERRORCODE.CHATROOM_TOP_EXIST.getIndex(),
					ERRORCODE.CHATROOM_TOP_EXIST.getReason());
		} else {
			chatroomsTop.add(chatroomid);
		}
		userBo.setChatrooms(chatrooms);
		userBo.setChatroomsTop(chatroomsTop);
		userService.updateChatrooms(userBo);
		userService.updateChatroomsTop(userBo);
		return Constant.COM_RESP;
	}

	@RequestMapping("/cancel-top")
	@ResponseBody
	public String cancelTop(String chatroomid, HttpServletRequest request,
			HttpServletResponse response) {
		HttpSession session = request.getSession();
		if (session.isNew()) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		if (session.getAttribute("isLogin") == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		UserBo userBo = (UserBo) session.getAttribute("userBo");
		if (userBo == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		userBo = userService.getUser(userBo.getId());
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (null == chatroomBo) {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		HashSet<String> chatrooms = userBo.getChatrooms();
		LinkedHashSet<String> chatroomsTop = userBo.getChatroomsTop();
		if (chatroomsTop.contains(chatroomid)) {
			chatroomsTop.remove(chatroomid);
		} else {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		if (chatrooms.contains(chatroomid)) {
			return CommonUtil.toErrorResult(
					ERRORCODE.CHATROOM_EXIST.getIndex(),
					ERRORCODE.CHATROOM_EXIST.getReason());
		} else {
			chatrooms.add(chatroomid);
		}
		userBo.setChatrooms(chatrooms);
		userBo.setChatroomsTop(chatroomsTop);
		userService.updateChatrooms(userBo);
		userService.updateChatroomsTop(userBo);
		return Constant.COM_RESP;
	}

	@RequestMapping("/factoface-create")
	@ResponseBody
	public String faceToFaceCreate(final int seq, double px, double py,
			HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		if (session.isNew()) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		if (session.getAttribute("isLogin") == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		UserBo userBo = (UserBo) session.getAttribute("userBo");
		if (userBo == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		userBo = userService.getUser(userBo.getId());
		HashSet<String> userSet = new HashSet<String>();
		ChatroomBo chatroomBo = null;
		int isNew = 0;
		synchronized (this) {
			chatroomBo = chatroomService.selectBySeq(seq);
			if (null == chatroomBo) {
				chatroomBo = new ChatroomBo();
				chatroomBo.setSeq(seq);
				chatroomBo.setName("chatFaceToFace");
				chatroomBo.setType(3);
				userSet.add(userBo.getId());
				chatroomBo.setUsers(userSet);
				chatroomService.insert(chatroomBo);
				isNew = 1;
			} else {
				if (0 == chatroomBo.getExpire()) {
					return CommonUtil.toErrorResult(
							ERRORCODE.CHATROOM_SEQ_EXPIRE.getIndex(),
							ERRORCODE.CHATROOM_SEQ_EXPIRE.getReason());
				}
				userSet.add(userBo.getId());
				chatroomBo.setUsers(userSet);
				chatroomService.updateUsers(chatroomBo);
			}
		}
		HashSet<String> chatrooms = userBo.getChatrooms();
		if (null == chatrooms) {
			chatrooms = new HashSet<String>();
		}
		chatrooms.add(chatroomBo.getId());
		userBo.setChatrooms(chatrooms);
		userService.updateChatrooms(userBo);
		if (isNew == 1) {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				public void run() {
					chatroomService.setSeqExpire(seq);
				}
			}, 5000);
			ImAssistant assistent = ImAssistant.init("180.76.138.200", 2222);
			if (assistent == null) {
				return CommonUtil.toErrorResult(
						ERRORCODE.PUSHED_CONNECT_ERROR.getIndex(),
						ERRORCODE.PUSHED_CONNECT_ERROR.getReason());
			}
			IMTermBo iMTermBo = iMTermService.selectByUserid(userBo.getId());
			if (iMTermBo == null) {
				iMTermBo = new IMTermBo();
				iMTermBo.setUserid(userBo.getId());
				Message message = assistent.getAppKey();
				String appKey = message.getMsg();
				Message message2 = assistent.authServer(appKey);
				String term = message2.getMsg();
				iMTermBo.setTerm(term);
				iMTermService.insert(iMTermBo);
			}
			assistent.setServerTerm(iMTermBo.getTerm());
			Message message3 = assistent.subscribe(chatroomBo.getName(),
					chatroomBo.getId(), userBo.getId());
			if (message3.getStatus() == Message.Status.termError) {
				Message message = assistent.getAppKey();
				String appKey = message.getMsg();
				Message message2 = assistent.authServer(appKey);
				String term = message2.getMsg();
				iMTermService.updateByUserid(userBo.getId(), term);
				assistent.setServerTerm(term);
				Message message4 = assistent.subscribe(chatroomBo.getName(),
						chatroomBo.getId(), userBo.getId());
				if (Message.Status.success != message4.getStatus()) {
					assistent.close();
					return CommonUtil.toErrorResult(message4.getStatus(),
							message4.getMsg());
				}
			} else if (Message.Status.success != message3.getStatus()) {
				assistent.close();
				return CommonUtil.toErrorResult(message3.getStatus(),
						message3.getMsg());
			}
			assistent.close();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("channelId", chatroomBo.getId());
		return JSONObject.fromObject(map).toString();
	}
}
