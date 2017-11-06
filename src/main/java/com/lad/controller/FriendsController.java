package com.lad.controller;

import com.lad.bo.*;
import com.lad.service.*;
import com.lad.util.*;
import com.lad.vo.FriendsVo;
import com.lad.vo.UserBaseVo;
import com.lad.vo.UserVoFriends;
import com.pushd.ImAssistant;
import com.pushd.Message;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
@RequestMapping("friends")
public class FriendsController extends BaseContorller {

	@Autowired
	private IFriendsService friendsService;
	@Autowired
	private IChatroomService chatroomService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IIMTermService iMTermService;

	@Autowired
	private ITagService tagService;

	@RequestMapping("/apply")
	@ResponseBody
	public String apply(String friendid, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		UserBo friendBo = userService.getUser(friendid);
		if (friendBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		FriendsBo temp = friendsService.getFriendByIdAndVisitorId(
				userBo.getId(), friendid);
		if (temp != null) {
			if (temp.getApply() == 1) {
				return CommonUtil.toErrorResult(
						ERRORCODE.FRIEND_EXIST.getIndex(),
						ERRORCODE.FRIEND_EXIST.getReason());
			}
			return CommonUtil.toErrorResult(
					ERRORCODE.FRIEND_APPLY_EXIST.getIndex(),
					ERRORCODE.FRIEND_APPLY_EXIST.getReason());
		}
		FriendsBo friendsBo = new FriendsBo();
		friendsBo.setUserid(userBo.getId());
		friendsBo.setFriendid(friendid);
		friendsBo.setBackname(friendBo.getUserName());
		friendsBo.setApply(0);
		friendsService.insert(friendsBo);
		JPushUtil.pushTo(userBo.getUserName() + JPushUtil.APPLY, friendid);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 *
	 * @param id  好友关系中的ID，不是userid或者朋友ID
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/agree")
	@ResponseBody
	public String agree(String id, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		if (StringUtils.isEmpty(id)) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		FriendsBo friendsBo = friendsService.get(id);
		if (friendsBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		String userid = friendsBo.getFriendid();
		String friendid = friendsBo.getUserid();
		UserBo friendBo = userService.getUser(friendid);
		if (friendBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		if (!userid.equals(userBo.getId())) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_ERROR.getIndex(),
					ERRORCODE.FRIEND_ERROR.getReason());
		}
		FriendsBo temp = friendsService.getFriendByIdAndVisitorId(
				userid, friendid);
		if (temp != null) {
			friendsService.updateApply(temp.getId(), 1);
		} else {
			//更新同意人的好友信息
			FriendsBo friendsBo2 = new FriendsBo();
			friendsBo2.setUserid(userid);
			friendsBo2.setFriendid(friendid);
			friendsBo2.setBackname(friendBo.getUserName());
			friendsBo2.setApply(1);
			friendsService.insert(friendsBo2);
		}
		//更新申请人的好友信息
		friendsService.updateApply(id, 1);

		ChatroomBo chatroomBo = chatroomService.selectByUserIdAndFriendid(
				userid, friendid);
		boolean isNew = false;
		UserBo user = userService.getUser(userid);
		UserBo friend = userService.getUser(friendid);
		//在聊天室中，用户ID和好友ID是一对，所以互换ID能够查询到，都算同一个channel
		if (null == chatroomBo) {
			chatroomBo = chatroomService.selectByUserIdAndFriendid(
					friendid, userid);
			if (null == chatroomBo){
				isNew = true;
			}
		}
		chatroomBo = savekUserAndFriendChatroom(user, friend, chatroomBo);

		IMTermBo iMTermBo = iMTermService.selectByUserid(userBo.getId());
		String term = "";
		if (iMTermBo != null) {
			term = iMTermBo.getTerm();
		} else {
			iMTermBo = new IMTermBo();
			iMTermBo.setTerm(term);
			iMTermBo.setUserid(userid);
			iMTermService.insert(iMTermBo);
		}
		//是不是创建聊天室
		int type = isNew ? 0 : 1;
		String[] res = IMUtil.subscribe(type,chatroomBo.getId(), term, userid, friendid);
		if (!res[0].equals(IMUtil.FINISH)) {
			return res[0];
		}
		iMTermService.updateByUserid(userid, res[1]);
		
		JPushUtil.pushTo(userBo.getUserName() + JPushUtil.AGREE_APPLY_FRIEND,
				friendid);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("channelId", chatroomBo.getId());
		return JSONObject.fromObject(map).toString();
	}


	/**
	 * 判断并保存用户与好友之间的chatroom关系
	 * @param user 本人
	 * @param friend 好友
	 * @param chatroomBo 聊天室
	 * @return
	 */
	private ChatroomBo savekUserAndFriendChatroom(UserBo user, UserBo friend, ChatroomBo chatroomBo){
		if (null == chatroomBo) {
			chatroomBo = new ChatroomBo();
			chatroomBo.setType(1);
			chatroomBo.setName(friend.getUserName());
			chatroomBo.setUserid(user.getId());
			chatroomBo.setFriendid(friend.getId());
			chatroomService.insert(chatroomBo);
		}
		HashSet<String> userChatrooms = user.getChatrooms();
		HashSet<String> friendChatrooms = friend.getChatrooms();
		userChatrooms.add(chatroomBo.getId());
		friendChatrooms.add(chatroomBo.getId());
		user.setChatrooms(userChatrooms);
		friend.setChatrooms(friendChatrooms);
		userService.updateChatrooms(user);
		userService.updateChatrooms(friend);
		return chatroomBo;
	}


	@RequestMapping("/refuse")
	@ResponseBody
	public String refuse(String id, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		if (StringUtils.isEmpty(id)) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		FriendsBo friendsBo = friendsService.get(id);
		if (friendsBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		friendsService.updateApply(id, -1);
		JPushUtil.pushTo(userBo.getUserName() + JPushUtil.REFUSE_APPLY_FRIEND,
				friendsBo.getUserid());
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/apply-list")
	@ResponseBody
	public String applyList(HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		List<FriendsBo> friendsBoList = friendsService
				.getApplyFriendByuserid(userBo.getId());
		List<UserVoFriends> userVoList = new LinkedList<UserVoFriends>();
		for (FriendsBo friendsBo : friendsBoList) {
			UserBo userBoTemp = userService.getUser(friendsBo.getUserid());
			if (null == userBoTemp) {
				return CommonUtil.toErrorResult(
						ERRORCODE.FRIEND_DATA_ERROR.getIndex(),
						ERRORCODE.FRIEND_DATA_ERROR.getReason());
			}
			UserVoFriends user = new UserVoFriends();
			BeanUtils.copyProperties(userBoTemp, user);
			user.setFriendsTableId(friendsBo.getId());
			userVoList.add(user);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("userVoList", userVoList);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/set-VIP")
	@ResponseBody
	public String setVIP(String friendid, Integer VIP,
			HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		if (StringUtils.isEmpty(friendid)) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		if (null == VIP) {
			return CommonUtil.toErrorResult(
					ERRORCODE.FRIEND_VIP_NULL.getIndex(),
					ERRORCODE.FRIEND_VIP_NULL.getReason());
		}
		FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(
				userBo.getId(), friendid);
		if (friendsBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		friendsBo.setVIP(VIP);
		friendsService.updateVIP(friendsBo.getUserid(),
				friendsBo.getFriendid(), VIP);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/set-black")
	@ResponseBody
	public String setBlack(String friendid, Integer black,
			HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		if (StringUtils.isEmpty(friendid)) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		if (null == black) {
			return CommonUtil.toErrorResult(
					ERRORCODE.FRIEND_BLACK_NULL.getIndex(),
					ERRORCODE.FRIEND_BLACK_NULL.getReason());
		}
		FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(
				userBo.getId(), friendid);
		if (friendsBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		friendsBo.setBlack(black);
		friendsService.updateBlack(friendsBo.getUserid(),
				friendsBo.getFriendid(), black);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/set-backname")
	@ResponseBody
	public String setBackName(String friendid, String backname,
			HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		if (StringUtils.isEmpty(friendid)) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		if (StringUtils.isEmpty(backname)) {
			return CommonUtil.toErrorResult(
					ERRORCODE.FRIEND_BACKNAME_NULL.getIndex(),
					ERRORCODE.FRIEND_BACKNAME_NULL.getReason());
		}
		FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(
				userBo.getId(), friendid);
		if (friendsBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		friendsService.updateBackname(userBo.getId(),
				friendsBo.getFriendid(), backname);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/set-phone")
	@ResponseBody
	public String setPhone(String friendid, String phone,
			HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		if (StringUtils.isEmpty(friendid)) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		if (null == phone) {
			return CommonUtil.toErrorResult(
					ERRORCODE.FRIEND_PHONE_NULL.getIndex(),
					ERRORCODE.FRIEND_PHONE_NULL.getReason());
		}
		FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(
				userBo.getId(), friendid);
		if (friendsBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		HashSet<String> phones = friendsBo.getPhone();
		String[] phoneArr = phone.split(",");
		for (String str : phoneArr) {
			phones.add(str);
		}
		friendsService.updatePhone(friendsBo.getUserid(),
				friendsBo.getFriendid(), phones);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/set-description")
	@ResponseBody
	public String setDescription(String friendid, String description,
			HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		if (StringUtils.isEmpty(friendid)) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		if (null == description) {
			return CommonUtil.toErrorResult(
					ERRORCODE.FRIEND_PHONE_NULL.getIndex(),
					ERRORCODE.FRIEND_PHONE_NULL.getReason());
		}
		FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(
				userBo.getId(), friendid);
		if (friendsBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		friendsBo.setDescription(description);
		friendsService.updateDescription(friendsBo.getUserid(),
				friendsBo.getFriendid(), description);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/get-friends")
	@ResponseBody
	public String getFriends(HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		String userid = userBo.getId();
		List<FriendsBo> list = friendsService.getFriendByUserid(userid);
		List<FriendsVo> voList = new LinkedList<FriendsVo>();
		for (FriendsBo friendsBo : list) {
			FriendsVo vo = new FriendsVo();
			ChatroomBo chatroomBo = chatroomService.selectByUserIdAndFriendid(
					userBo.getId(), friendsBo.getFriendid());
			if (chatroomBo == null) {
				chatroomBo = chatroomService.selectByUserIdAndFriendid(
						 friendsBo.getFriendid(), userBo.getId());
				if (chatroomBo == null) {
					UserBo friend = userService.getUser(friendsBo.getFriendid());
					chatroomBo = savekUserAndFriendChatroom(userBo, friend, chatroomBo);
					IMTermBo iMTermBo = iMTermService.selectByUserid(userid);
					String term = "";
					if (iMTermBo == null) {
						iMTermBo = new IMTermBo();
						iMTermBo.setTerm(term);
						iMTermBo.setUserid(userid);
						iMTermService.insert(iMTermBo);
					} else {
						term = iMTermBo.getTerm();
					}
					//首次创建聊天室，需要输入名称
					String[] res = IMUtil.subscribe(0, chatroomBo.getId(), term, userid, friend.getId());

					if (!res[0].equals(IMUtil.FINISH)) {
						return res[0];
					}
					iMTermService.updateByUserid(userid, res[1]);
				}
			}

			BeanUtils.copyProperties(friendsBo, vo);
			String friendid = friendsBo.getFriendid();
			UserBo friend = userService.getUser(friendid);
			List<TagBo> tagBos = tagService.getTagBoListByUseridAndFrinedid(userid, friendid);
			List<String> tagList = new ArrayList<>();
			for (TagBo tagBo : tagBos) {
				tagList.add(tagBo.getName());
			}
			vo.setTag(tagList);
			vo.setUsername(friend.getUserName());
			vo.setPicture(friend.getHeadPictureName());
			vo.setChannelId(chatroomBo.getId());
			if (StringUtils.isEmpty(friendsBo.getBackname())) {
				vo.setBackname(friend.getUserName());
			} else {
				vo.setBackname(friendsBo.getBackname());
			}
			voList.add(vo);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("tag", voList);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/delete")
	@ResponseBody
	public String delete(String friendid, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		if (StringUtils.isEmpty(friendid)) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		ChatroomBo chatroomBo = chatroomService.selectByUserIdAndFriendid(
				userBo.getId(), friendid);
		if (null == chatroomBo) {
			chatroomBo = chatroomService.selectByUserIdAndFriendid(
					friendid,userBo.getId());
		}
		if (chatroomBo != null) {
			String result = IMUtil.disolveRoom(iMTermService, userBo.getId(),
					chatroomBo.getId());
			if (!result.equals(IMUtil.FINISH)) {
				return result;
			}
			//删除好友互相设置信息user
			chatroomService.deleteChatroomUser(userBo.getId(),chatroomBo.getId());
			chatroomService.deleteChatroomUser(friendid,chatroomBo.getId());
		}
		FriendsBo temp = friendsService.getFriendByIdAndVisitorId(
				userBo.getId(), friendid);
		if (temp != null) {
			friendsService.delete(userBo.getId(), friendid);
		}
		//在添加好友的会互换id保存
		temp = friendsService.getFriendByIdAndVisitorId(friendid,
				userBo.getId());
		if (temp != null ) {
			friendsService.delete(friendid, userBo.getId());
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/multi-insert")
	@ResponseBody
	public String multiInsert(String friendids, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		if (StringUtils.isEmpty(friendids)) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		if (!friendids.contains(",")) {
			return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(),
					ERRORCODE.FRIEND_NULL.getReason());
		}
		ImAssistant assistent = ImAssistant.init(Constant.PUSHD_IP, Constant.PUSHD_POST);
		if (assistent == null) {
			return CommonUtil.toErrorResult(
					ERRORCODE.PUSHED_CONNECT_ERROR.getIndex(),
					ERRORCODE.PUSHED_CONNECT_ERROR.getReason());
		}
		String term = getTerm(assistent);
		if ("timeout".equals(term)){
			return CommonUtil.toErrorResult(
					ERRORCODE.PUSHED_CONNECT_ERROR.getIndex(),
					ERRORCODE.PUSHED_CONNECT_ERROR.getReason());
		}
		String[] idsList = friendids.split(",");
		LinkedHashSet<String> userSet = new LinkedHashSet<String>();
		for (String id : idsList) {
			FriendsBo temp = friendsService.getFriendByIdAndVisitorIdAgree(
					userBo.getId(), id);
			if (temp == null) {
				if (id.equals(userBo.getId())) {
					userSet.add(id);
					continue;
				}
				return CommonUtil.toErrorResult(
						ERRORCODE.FRIEND_NULL.getIndex(),
						ERRORCODE.FRIEND_NULL.getReason());
			}
			userSet.add(id);
		}
		userSet.add(userBo.getId());
		ChatroomBo chatroomBo = new ChatroomBo();
		chatroomBo.setType(2);
		chatroomBo.setName("群聊");
		chatroomBo.setUsers(userSet);
		chatroomBo.setMaster(userBo.getId());
		chatroomBo.setCreateuid(userBo.getId());
		chatroomService.insert(chatroomBo);
		for (String id : userSet) {
			UserBo user = userService.getUser(id);
			HashSet<String> chatroomsSet = user.getChatrooms();
			chatroomsSet.add(chatroomBo.getId());
			user.setChatrooms(chatroomsSet);
			userService.updateChatrooms(user);
			addChatroomUser(user, chatroomBo.getId());
		}
		IMTermBo iMTermBo = iMTermService.selectByUserid(userBo.getId());
		if (iMTermBo == null) {
			iMTermBo = new IMTermBo();
			iMTermBo.setUserid(userBo.getId());
			iMTermBo.setTerm(term);
			iMTermService.insert(iMTermBo);
		}
		assistent.setServerTerm(iMTermBo.getTerm());
		Message message = assistent.createChatRoom(chatroomBo.getId(), idsList);
		if (message.getStatus() == Message.Status.termError) {
			term = getTerm(assistent);
			iMTermService.updateByUserid(userBo.getId(), term);
			assistent.setServerTerm(term);
			message = assistent.createChatRoom(chatroomBo.getId(), idsList);
			if (Message.Status.success != message.getStatus()) {
				assistent.close();
				return CommonUtil.toErrorResult(message.getStatus(),
						message.getMsg());
			}
		} else if (Message.Status.success != message.getStatus()) {
			assistent.close();
			return CommonUtil.toErrorResult(message.getStatus(),
					message.getMsg());
		}
		JPushUtil
				.pushTo(userBo.getUserName() + JPushUtil.MULTI_INSERT, idsList);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("channelId", chatroomBo.getId());
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 添加聊天室用户
	 * @param chatroomid
	 */
	@Async
	private void addChatroomUser(UserBo userBo, String chatroomid){
		ChatroomUserBo chatroomUserBo = chatroomService.findChatUserByUserAndRoomid(userBo.getId(), chatroomid);
		if (chatroomUserBo == null) {
			chatroomUserBo = new ChatroomUserBo();
			chatroomUserBo.setChatroomid(chatroomid);
			chatroomUserBo.setUserid(userBo.getId());
			chatroomUserBo.setUsername(userBo.getUserName());
			chatroomUserBo.setShowNick(false);
			chatroomUserBo.setDisturb(false);
			chatroomService.insertUser(chatroomUserBo);
		} else {
			chatroomService.updateUserNickname(chatroomUserBo.getId(), "");
		}
	}

	private String getTerm(ImAssistant assistent){
		Message message = assistent.getAppKey();
		String appKey = message.getMsg();
		message = assistent.authServer(appKey);
		return message.getMsg();
	}

	@RequestMapping("/multi-out")
	@ResponseBody
	public String multiOut(String chatroomid, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (null == chatroomBo) {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		String userid = userBo.getId();
		LinkedHashSet<String> userids = chatroomBo.getUsers();
		if (userid.equals(chatroomBo.getMaster())) {
			  if (userids.size() > 2) {
				  return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_MASTER_QUIT.getIndex(),
						  ERRORCODE.CHATROOM_MASTER_QUIT.getReason());
			  }
		}
		userBo = userService.getUser(userid);
		HashSet<String> chatrooms = userBo.getChatrooms();
		IMTermBo termBo = iMTermService.selectByUserid(userid);
		String term = termBo == null ? "" : termBo.getTerm();
		String[] res = IMUtil.unSubscribe(chatroomid, term, userid);
		if (!res[0].equals(IMUtil.FINISH)) {
			return res[0];
		}
		if (chatrooms.contains(chatroomid)) {
			chatrooms.remove(chatroomid);
		}
		userBo.setChatrooms(chatrooms);
		userService.updateChatrooms(userBo);
		if (userids.contains(userid)) {
			userids.remove(userid);
		}
		if (userids.size() <= 2) {
			chatroomService.delete(chatroomid);
		} else {
			chatroomBo.setUsers(userids);
			chatroomService.updateUsers(chatroomBo);
		}
		return Constant.COM_RESP;
	}



	@RequestMapping("/sign-users")
	@ResponseBody
	public String signUsers(String[] phones, HttpServletRequest request, HttpServletResponse response) {
		List<UserBaseVo> userBaseVos = new ArrayList<>();
		if(null != phones) {
			for (String phone : phones) {
				UserBo user = userService.getUserByPhone(phone);
				if(null != user) {
					UserBaseVo baseVo = new UserBaseVo();
					org.springframework.beans.BeanUtils.copyProperties(user, baseVo);
					userBaseVos.add(baseVo);
				} 
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("userVos", userBaseVos);
		return JSONObject.fromObject(map).toString();
	}
}
