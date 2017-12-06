package com.lad.controller;

import com.lad.bo.*;
import com.lad.redis.RedisServer;
import com.lad.service.IChatroomService;
import com.lad.service.IFriendsService;
import com.lad.service.IReasonService;
import com.lad.service.IUserService;
import com.lad.util.*;
import com.lad.vo.ChatroomUserVo;
import com.lad.vo.ChatroomVo;
import com.lad.vo.ReasonVo;
import com.lad.vo.UserVo;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.api.RLock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("chatroom")
public class ChatroomController extends BaseContorller {

	private static Logger logger = LogManager.getLogger(ChatroomController.class);

	@Autowired
	private IChatroomService chatroomService;
	@Autowired
	private IUserService userService;

	@Autowired
	private RedisServer redisServer;

	@Autowired
	private IReasonService reasonService;

	@Autowired
	private IFriendsService friendsService;


	private String titlePush = "互动通知";

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
		ChatroomBo chatroomBo = new ChatroomBo();
		chatroomBo.setName(userBo.getUserName());
		chatroomBo.setType(Constant.ROOM_MULIT);
		chatroomBo.setCreateuid(userBo.getId());
		chatroomBo.setMaster(userBo.getId());
		HashSet<String> users = chatroomBo.getUsers();
		users.add(userBo.getId());
		chatroomService.insert(chatroomBo);
		//第一个为返回结果信息，第二位term信息
		String result = IMUtil.subscribe(0, chatroomBo.getId(), userBo.getId());
		if (!result.equals(IMUtil.FINISH)) {
			chatroomService.remove(chatroomBo.getId());
			return result;
		}
		userService.updateChatrooms(userBo);
		addChatroomUser(chatroomService, userBo, chatroomBo.getId(), userBo.getUserName());
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("channelId", chatroomBo.getId());
		return JSONObject.fromObject(map).toString();
	}


	@RequestMapping("/insert-user")
	@ResponseBody
	public String insertUser(String userids, String chatroomid,
							 HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		if (StringUtils.isEmpty(userids)) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_ID.getIndex(),
					ERRORCODE.ACCOUNT_ID.getReason());
		}
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (null == chatroomBo) {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		if (!chatroomBo.isOpen()) {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NOT_OPEN.getIndex(),
					ERRORCODE.CHATROOM_NOT_OPEN.getReason());
		}
		//判断参数是一个还是多个
		String[] useridArr;
		if (userids.indexOf(',') > 0) {
			useridArr = userids.trim().split(",");
		} else {
			useridArr = new String[]{userids};
		}
		if (chatroomBo.isVerify() && !chatroomBo.getMaster().equals(userBo.getId())) {
			String path = "";
			String content = String.format("%s邀请您加入群聊", userBo.getUserName());
			JPushUtil.push(titlePush, content, path,  useridArr);
			return Constant.COM_RESP;
		}
		//第一个为返回结果信息，第二位term信息
		String result = IMUtil.subscribe(1,chatroomid, useridArr);
		if (!result.equals(IMUtil.FINISH)) {
			return result;
		}

		// 为IMUtil通知做数据准备
		LinkedHashSet<String> set = chatroomBo.getUsers();
		String[] tt = new String[set.size() -1];
		int i=0;
		for(String uu: set){
			if(uu.equals(userBo.getId())) continue;
			tt[i++] = uu;
		}
		Object[] otherNameAndId = ChatRoomUtil.getUserNamesAndIds(userService, tt, logger);
		ArrayList<String> imNames = new ArrayList<>();
		ArrayList<String> imIds = new ArrayList<>();

		for (String userid : useridArr) {
			if (set.contains(userid)) {
				continue;
			}
			UserBo user = userService.getUser(userid);
			if (null != user) {
				addChatroomUser(chatroomService, user, chatroomBo.getId(), user.getUserName());
				HashSet<String> chatroom = user.getChatrooms();
				//个人聊天室中没有当前聊天室，则添加到个人的聊天室
				if (!chatroom.contains(chatroomBo.getId())) {
					chatroom.add(chatroomBo.getId());
					user.setChatrooms(chatroom);
					userService.updateChatrooms(user);
				}

				set.add(userid);
				imNames.add(user.getUserName());
				imIds.add(user.getId());

				JPushUtil.pushTo(String.format("%s邀请您加入群聊", userBo.getUserName()), userid);

			}
		}
		// 如果群聊没有修改过名称，自动修改名称
		String name = chatroomBo.getName();
		if(!chatroomBo.isNameSet()){
			String newChatRoomName = ChatRoomUtil.generateChatRoomName(userService, set, chatroomid, logger);
			if(newChatRoomName != null){
				name = newChatRoomName;
				chatroomService.updateName(chatroomid, newChatRoomName, false);
			}
		}
		chatroomBo.setUsers(set);
		chatroomService.updateUsers(chatroomBo);

		addRoomInfo(userBo, chatroomid, imIds, imNames, otherNameAndId);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("channelId", chatroomBo.getId());
		map.put("chatroomUser", set.size());
		map.put("chatroomName", name);
		return JSONObject.fromObject(map).toString();
	}


	@RequestMapping("/delete-user")
	@ResponseBody
	public String deltetUser(String userids, String chatroomid,
							 HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		if (StringUtils.isEmpty(userids)) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_ID.getIndex(),
					ERRORCODE.ACCOUNT_ID.getReason());
		}
		String[] useridArr = CommonUtil.getIds(userids.trim());
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (null == chatroomBo) {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		if (!userBo.getId().equals(chatroomBo.getMaster())) {
			return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_NOT_MASTER.getIndex(),
					ERRORCODE.CIRCLE_NOT_MASTER.getReason());
		}

		/**
		 * TODO 发通知成功，后面失败了如何处理
		 *
		 * 之所以先发通知，是因为调用了IMUtil.unSubscribe这个后，订阅的关系都解除了，就没法基于群聊发通知了，被踢出的人就不能收到消息
		 */
		Object[] nameAndIds = ChatRoomUtil.getUserNamesAndIds(userService, useridArr, logger);
		if (nameAndIds[0] != null){
			JSONObject json = new JSONObject();
			json.put("masterId", userBo.getId());
			json.put("masterName", userBo.getUserName());
			json.put("hitIds", nameAndIds[1]);
			json.put("hitNames", nameAndIds[0]);

			// 向群中发踢人通知
			String res = IMUtil.notifyInChatRoom(Constant.SOME_ONE_EXPELLED_FROM_CHAT_ROOM, chatroomid, json.toString());
			if(!IMUtil.FINISH.equals(res)){
				logger.error("failed notifyInChatRoom Constant.SOME_ONE_EXPELLED_FROM_CHAT_ROOM, %s",res);
				return res;
			}
		}

		//第一个为返回结果信息，第二位term信息
		String result = IMUtil.unSubscribe(chatroomid, useridArr);
		if (!result.equals(IMUtil.FINISH) && !result.contains("not found")) {
			return result;
		}

		StringBuilder imIds = new StringBuilder();
		StringBuilder imNames = new StringBuilder();

		LinkedHashSet<String> set = chatroomBo.getUsers();
		for (String userid : useridArr) {
			//只能删除非自己以外人员，自己需要退出
			if (!set.contains(userid) || userid.equals(userBo.getId())) {
				continue;
			}

			set.remove(userid);
			UserBo user = userService.getUser(userid);
			if (null != user) {
				updateFriendChatroom(user, chatroomid);

				imIds.append(user.getId());
				imIds.append(",");

				imNames.append(user.getUserName());
				imNames.append(",");
			}
		}
		String name = chatroomBo.getName();

		//聊天室少于2人则直接删除
		if (set.size() < 2) {
			String res = IMUtil.disolveRoom(chatroomid);
			if (!res.equals(IMUtil.FINISH) && !res.contains("not found")) {
				return res;
			}
			//删除最后一人的聊天室
			if (set.size() == 1) {
				String friendid = set.iterator().next();
				UserBo friend = userService.getUser(friendid);
				if (friend != null){
					updateFriendChatroom(friend, chatroomid);
				}
			}
			chatroomService.delete(chatroomid);

		} else {
			// 如果群聊没有修改过名称，自动修改名称
			if(!chatroomBo.isNameSet()){
				String newChatRoomName = ChatRoomUtil.generateChatRoomName(userService, set, chatroomid, logger);
				if(newChatRoomName != null){
					name = newChatRoomName;
					chatroomService.updateName(chatroomid, newChatRoomName, false);
				}
			}
			chatroomBo.setUsers(set);
			chatroomService.updateUsers(chatroomBo);
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("channelId", chatroomBo.getId());
		map.put("chatroomUser", set.size());
		map.put("chatroomName", name);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/quit")
	@ResponseBody
	public String quit(String chatroomid, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (null == chatroomBo) {
			updateUserChatroom(userBo, chatroomid);
			return Constant.COM_RESP;
		}

		/**
		 * TODO 发通知成功，后面失败了如何处理
		 *
		 * 之所以先发通知，是因为调用了IMUtil.unSubscribe这个后，订阅的关系都解除了，就没法基于群聊发通知了，被踢出的人就不能收到消息
		 * 目前先这样处理，之后需要添加基于个人的发送通知功能
		 */
		// 向群中发某人退出群聊通知
		JSONObject json = new JSONObject();
		json.put("masterId", userBo.getId());
		json.put("masterName", userBo.getUserName());
		String res2 = IMUtil.notifyInChatRoom(Constant.SOME_ONE_QUIT_CHAT_ROOM, chatroomid, json.toString());
		if(!IMUtil.FINISH.equals(res2)){
			logger.error("failed notifyInChatRoom Constant.SOME_ONE_QUIT_CHAT_ROOM, %s",res2);
			return res2;
		}

		String userid = userBo.getId();
		//第一个为返回结果信息，第二位term信息
		String result = IMUtil.unSubscribe(chatroomid, userid);
		if (!result.equals(IMUtil.FINISH) && !result.contains("not found")) {
			return result;
		}
		LinkedHashSet<String> set = chatroomBo.getUsers();
		if (set.size() >= 2) {
			//如果是群主退出，则由下一个人担当
			if (userid.equals(chatroomBo.getMaster())) {
				set.remove(userid);
				String nextId = set.iterator().next();
				chatroomService.updateMaster(chatroomid, nextId);

				// 通知群主变更通知
				UserBo nextMaster = userService.getUser(nextId);
				if(nextMaster != null){
					JSONObject json2 = new JSONObject();
					json2.put("masterId", nextMaster.getId());
					json2.put("masterName", nextMaster.getUserName());
					String res3 = IMUtil.notifyInChatRoom(Constant.MASTER_CHANGE_CHAT_ROOM, chatroomid, json2.toString());
					if(!IMUtil.FINISH.equals(res3)){
						logger.error("failed notifyInChatRoom Constant.SOME_ONE_QUIT_CHAT_ROOM, %s",res3);
						return res3;
					}
				}
			}
		}
		updateUserChatroom(userBo, chatroomid);
		deleteNickname(userid, chatroomid);
		set.remove(userid);

		String name = chatroomBo.getName();

		if (set.size() < 2) {
			String res = IMUtil.disolveRoom(chatroomid);
			if (!res.equals(IMUtil.FINISH) && !res.contains("not found")) {
				return res;
			}
			//删除最后一人的聊天室
			if (set.size() == 1) {
				String friendid = set.iterator().next();
				UserBo friend = userService.getUser(friendid);
				if (friend != null){
					updateFriendChatroom(friend, chatroomid);
				}
			}
			chatroomService.delete(chatroomid);

		} else {
			// 如果群聊没有修改过名称，自动修改名称
			if(!chatroomBo.isNameSet()){
				String newChatRoomName = ChatRoomUtil.generateChatRoomName(userService, set, chatroomid, logger);
				if(newChatRoomName != null){
					name = newChatRoomName;
					chatroomService.updateName(chatroomid, newChatRoomName, false);
				}
			}
			chatroomBo.setUsers(set);
			chatroomService.updateUsers(chatroomBo);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("channelId", chatroomBo.getId());
		map.put("chatroomUser", set.size());
		map.put("chatroomName", name);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 更新当前用户的聊天室
	 * @param userBo
	 * @param chatroomid
	 */
	private void updateUserChatroom(UserBo userBo, String chatroomid){
		HashSet<String> chatroom = userBo.getChatrooms();
		LinkedList<String> chatroomTops = userBo.getChatroomsTop();
		boolean hasRoom = false;
		if (chatroom.contains(chatroomid)){
			chatroom.remove(chatroomid);
			userBo.setChatrooms(chatroom);
			hasRoom = true;
		}
		if (chatroomTops.contains(chatroomid)){
			chatroomTops.remove(chatroomid);
			userBo.setChatroomsTop(chatroomTops);
			hasRoom = true;
		}
		if (hasRoom){
			userService.updateChatrooms(userBo);
			logger.info("user  {}  delete  chatroom  {}", userBo.getId(), chatroomid);
		}
		chatroomService.deleteChatroomUser(userBo.getId(), chatroomid);
	}

	/**
	 * 删除其他人的聊天室信息
	 * @param userBo
	 * @param chatroomid
	 */
	private void updateFriendChatroom(UserBo userBo, String chatroomid){
		RLock lock = redisServer.getRLock("deleteUser");
		try {
			lock.lock(3, TimeUnit.SECONDS);
			updateUserChatroom(userBo, chatroomid);
		} finally {
			lock.unlock();
		}
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
				BeanUtils.copyProperties(temp, vo);
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
			HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		String userid = userBo.getId();
		HashSet<String> chatrooms = userBo.getChatrooms();
		LinkedList<String> chatroomsTop = userBo.getChatroomsTop();
		List<ChatroomVo> chatroomList = new LinkedList<ChatroomVo>();
		HashSet<String> removes = new LinkedHashSet<>();
		LinkedList<String> removeTops = new LinkedList<>();
		for (String id : chatroomsTop) {
			ChatroomBo temp = chatroomService.get(id);
			if (null != temp) {
				ChatroomUserBo chatroomUserBo = chatroomService.findChatUserByUserAndRoomid(userid, id);
				boolean has = chatroomUserBo != null;
				ChatroomVo vo = new ChatroomVo();
				BeanUtils.copyProperties(temp, vo);
				if (temp.getType() != 1) {
					bo2vo(has && chatroomUserBo.isShowNick(),temp, vo);
					vo.setUserNum(temp.getUsers().size());
					vo.setShowNick(has && chatroomUserBo.isShowNick());
				}
				vo.setDisturb(has && chatroomUserBo.isDisturb());
				vo.setTop(1);
				addName(temp, vo);
				chatroomList.add(vo);
			} else {
				removeTops.add(id);
			}
		}
		for (String id : chatrooms) {
			ChatroomBo temp = chatroomService.get(id);
			if (null != temp) {
				ChatroomUserBo chatroomUserBo = chatroomService.findChatUserByUserAndRoomid(userid, id);
				boolean has = chatroomUserBo != null;
				ChatroomVo vo = new ChatroomVo();
				BeanUtils.copyProperties(temp, vo);
				if (temp.getType() != 1) {
					bo2vo(has && chatroomUserBo.isShowNick(), temp, vo);
					vo.setUserNum(temp.getUsers().size());
					vo.setShowNick(has && chatroomUserBo.isShowNick());
				}
				addName(temp, vo);
				chatroomList.add(vo);
				vo.setDisturb(has && chatroomUserBo.isDisturb());
			} else {
				removes.add(id);
			}
		}
		updateUserRoom(userBo, removes, removeTops);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("ChatroomList", chatroomList);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/my-chatrooms")
	@ResponseBody
	public String getChatrooms(String timestamp, HttpServletRequest request,
							   HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		String userid = userBo.getId();
		LinkedList<String> chatroomsTop = userBo.getChatroomsTop();
		List<ChatroomVo> chatroomList = new LinkedList<ChatroomVo>();
		HashSet<String> removes = new LinkedHashSet<>();
		LinkedList<String> removeTops = new LinkedList<>();

		Date times;
		try {
			times = CommonUtil.getDate(timestamp);
		} catch (ParseException e){
			return CommonUtil.toErrorResult(ERRORCODE.FORMAT_ERROR.getIndex(),
					ERRORCODE.FORMAT_ERROR.getReason());
		}

		List<ChatroomVo> chats = new LinkedList<>();

		List<ChatroomBo> chatroomBos = chatroomService.findMyChatrooms(userid, times);

		String timeStr = "";
		if (chatroomBos != null && !chatroomBos.isEmpty()) {
			ChatroomBo first = chatroomBos.get(0);
			timeStr = CommonUtil.getDateStr(first.getCreateTime(),"yyyy-MM-dd HH:mm:ss");
			for (ChatroomBo chatroomBo : chatroomBos) {
				ChatroomUserBo chatroomUserBo = chatroomService.findChatUserByUserAndRoomid(userid, chatroomBo.getId());
				boolean has = chatroomUserBo != null;
				ChatroomVo vo = new ChatroomVo();
				BeanUtils.copyProperties(chatroomBo, vo);
				if (chatroomBo.getType() != 1) {
					bo2vo(has && chatroomUserBo.isShowNick(),chatroomBo, vo);
					vo.setUserNum(chatroomBo.getUsers().size());
					vo.setShowNick(has && chatroomUserBo.isShowNick());
				}
				vo.setDisturb(has && chatroomUserBo.isDisturb());
				if (chatroomsTop.contains(chatroomBo.getId())) {
					vo.setTop(1);
					chatroomList.add(vo);
				} else {
					chats.add(vo);
				}
				addName(chatroomBo, vo);
			}
			chatroomList.addAll(chats);
			updateUserRoom(userBo, removes, removeTops);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("timestamp", StringUtils.isNotEmpty(timeStr) ? timeStr : timestamp);
		map.put("ret", 0);
		map.put("ChatroomList", chatroomList);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 临时聊天室名称
	 * @param chatroomBo
	 * @param chatroomVo
	 */
	private void addName(ChatroomBo chatroomBo, ChatroomVo chatroomVo){
		if (chatroomBo.getType() == 1 && StringUtils.isNotEmpty(chatroomBo.getTargetid())){
			UserBo userBo = userService.getUser(chatroomBo.getFriendid());
			if (userBo != null) {
				chatroomVo.setName(userBo.getUserName());
			}
		}
	}

	@Async
	private void updateUserRoom(UserBo userBo, HashSet<String> removes, LinkedList<String> removeTops){
		HashSet<String> chatrooms = userBo.getChatrooms();
		LinkedList<String> chatroomsTop = userBo.getChatroomsTop();
		chatroomsTop.removeAll(removeTops);
		chatrooms.removeAll(removes);
		userBo.setChatroomsTop(chatroomsTop);
		userBo.setChatrooms(chatrooms);
		userService.updateChatrooms(userBo);
	}

	private void bo2vo(boolean isShowNick, ChatroomBo chatroomBo, ChatroomVo vo){
		LinkedHashSet<ChatroomUserVo> userVos = vo.getUserVos();
		List<ChatroomUserBo> chatroomUserBos = chatroomService.findByUserRoomid(chatroomBo.getId());
		for (ChatroomUserBo chatroomUser : chatroomUserBos) {
			String userid = chatroomUser.getUserid();
			UserBo chatUser = userService.getUser(userid);
			if (chatUser == null) {
				chatroomService.deleteUser(chatroomUser.getId());
				continue;
			}
			ChatroomUserVo userVo = new ChatroomUserVo();
			userVo.setUserid(chatUser.getId());
			userVo.setUserPic(chatUser.getHeadPictureName());
			if (userid.equals(chatroomBo.getMaster())) {
				userVo.setRole(2);
			}
			String nickname = isShowNick ? chatroomUser.getNickname() : chatroomUser.getUsername();
			userVo.setNickname(nickname);
			userVos.add(userVo);
		}
	}

	@RequestMapping("/get-chatroom-info")
	@ResponseBody
	public String getChatroomInfo(@RequestParam String chatroomid,
			HttpServletRequest request, HttpServletResponse response){
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		ChatroomBo temp = chatroomService.get(chatroomid);
		ChatroomVo vo = new ChatroomVo();
		if (null != temp) {
			BeanUtils.copyProperties(temp,vo);
			ChatroomUserBo chatroomUserBo = chatroomService.findChatUserByUserAndRoomid(userBo.getId(), chatroomid);
			if (chatroomUserBo == null) {
				chatroomUserBo = new ChatroomUserBo();
				chatroomUserBo.setChatroomid(chatroomid);
				chatroomUserBo.setUserid(userBo.getId());
				chatroomUserBo.setUsername(userBo.getUserName());
				chatroomUserBo.setNickname(userBo.getUserName());
				chatroomUserBo.setShowNick(false);
				chatroomUserBo.setDisturb(false);
				chatroomService.insertUser(chatroomUserBo);
				vo.setDisturb(false);
			} else {
				if (temp.getType() != 1) {
					bo2vo(chatroomUserBo.isShowNick(), temp, vo);
					vo.setUserNum(temp.getUsers().size());
					vo.setShowNick(chatroomUserBo.isShowNick());
				} else {
					FriendsBo bo = friendsService.getFriendByIdAndVisitorIdAgree(temp.getUserid(), temp
							.getFriendid());
					if (bo != null) {
						String nickname = StringUtils.isEmpty(bo.getBackname())? bo.getUsername() : bo.getBackname();
						vo.setName(nickname);
					}
				}
				vo.setDisturb(chatroomUserBo.isDisturb());
			}
		} else {
			return CommonUtil.toErrorResult(
					ERRORCODE.CHATROOM_ID_NULL.getIndex(),
					ERRORCODE.CHATROOM_ID_NULL.getReason());
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("chatroom", vo);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/set-top")
	@ResponseBody
	public String setTop(String chatroomid, HttpServletRequest request,
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
		HashSet<String> chatrooms = userBo.getChatrooms();
		LinkedList<String> chatroomsTop = userBo.getChatroomsTop();
		if (chatrooms.contains(chatroomid)) {
			chatrooms.remove(chatroomid);
		}
		if (chatroomsTop.contains(chatroomid)) {
			chatroomsTop.remove(chatroomid);
		}
		chatroomsTop.add(0, chatroomid);
		userService.updateChatrooms(userBo);
		return Constant.COM_RESP;
	}

	@RequestMapping("/cancel-top")
	@ResponseBody
	public String cancelTop(String chatroomid, HttpServletRequest request,
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
		HashSet<String> chatrooms = userBo.getChatrooms();
		LinkedList<String> chatroomsTop = userBo.getChatroomsTop();
		if (chatroomsTop.contains(chatroomid)) {
			chatroomsTop.remove(chatroomid);
		}
		if (!chatrooms.contains(chatroomid)) {
			chatrooms.add(chatroomid);
		}
		userService.updateChatrooms(userBo);
		return Constant.COM_RESP;
	}

	@RequestMapping("/facetoface-create")
	@ResponseBody
	public String faceToFaceCreate(@RequestParam int seq, @RequestParam double px, @RequestParam double py,
			HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		RLock lock = redisServer.getRLock(Constant.CHAT_LOCK);
		boolean isNew = false;
		double[] position = new double[]{px,py};
		ChatroomBo chatroom = null;
		try {
			//10s自动解锁
			lock.lock(4, TimeUnit.SECONDS);
			chatroom = chatroomService.selectBySeqInTen(seq, position, 100);
			if (null == chatroom) {
				chatroom = getChatroomBo(seq, position, userBo);
				isNew = true;
			} else {
				LinkedHashSet<String> userSet = chatroom.getUsers();
				userSet.add(userBo.getId());
				chatroom.setUsers(userSet);
			}
			if (isNew) {
				chatroom.setName(userBo.getUserName());
                chatroomService.insert(chatroom);
            } else {
				// 如果群聊没有修改过名称，自动修改名称
				if(!chatroom.isNameSet()){
					String newChatRoomName = ChatRoomUtil.generateChatRoomName(userService,
							chatroom.getUsers(), chatroom.getId(), logger);
					if(newChatRoomName != null){
						chatroomService.updateName(chatroom.getId(), newChatRoomName, false);
					}
				}
                chatroomService.updateUsers(chatroom);
            }
		} finally {
			lock.unlock();
		}
		int type = isNew ? 0 : 1;
		String res = IMUtil.subscribe(type,chatroom.getId(), userBo.getId());
		logger.info("face  user {}, chatroom {},  res {}", userBo.getId(), chatroom.getId(), res);
		if (!res.equals(IMUtil.FINISH)) {
			//失败需要还原
			if (isNew) {
				chatroomService.remove(chatroom.getId());
			} else {
				LinkedHashSet<String> userSet = chatroom.getUsers();
				userSet.remove(userBo.getId());
				chatroom.setUsers(userSet);
				chatroomService.updateUsers(chatroom);
			}
			return res;
		}
		HashSet<String> chatrooms = userBo.getChatrooms();
		chatrooms.add(chatroom.getId());
		userBo.setChatrooms(chatrooms);
		userService.updateChatrooms(userBo);
		addChatroomUser(chatroomService, userBo, chatroom.getId(), userBo.getUserName());

		// 发送通知推送
		if(!isNew){
			LinkedHashSet<String> users = chatroom.getUsers();
			String[] tt = new String[users.size()-1];
			int i=0;
			for(String uu: users){
				if(uu.equals(userBo.getId())) continue;
				tt[i++] = uu;
			}
			Object[] otherNameAndId = ChatRoomUtil.getUserNamesAndIds(userService, tt, logger);

			if(otherNameAndId[0] != null){
				JSONObject json = new JSONObject();
				json.put("masterId", userBo.getId());
				json.put("masterName", userBo.getUserName());
				json.put("otherIds", otherNameAndId[1]);
				json.put("otherNames", otherNameAndId[0]);

				// 向群中发某人加入群聊通知
				String res2 = IMUtil.notifyInChatRoom(
						Constant.SOME_ONE_JOIN_CHAT_ROOM,
						chatroom.getId(),
						json.toString());
				if(!IMUtil.FINISH.equals(res2)){
					logger.error("failed notifyInChatRoom Constant.FACE_TO_FACE_SOME_ONE_JOIN_CHAT_ROOM, %s",res2);
				}
			}

		}else{

			JSONObject json = new JSONObject();
			json.put("masterId", userBo.getId());
			json.put("masterName", userBo.getUserName());
			json.put("number", seq);

			String res2 = IMUtil.notifyInChatRoom(
					Constant.FACE_TO_FACE_SOME_ONE_JOIN_CHAT_ROOM,
					chatroom.getId(),
					json.toString());
			if(!IMUtil.FINISH.equals(res2)){
				logger.error("failed notifyInChatRoom Constant.FACE_TO_FACE_SOME_ONE_JOIN_CHAT_ROOM, %s",res2);
			}
		}

		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("channelId", chatroom.getId());
		return JSONObject.fromObject(map).toString();
	}


	private ChatroomBo getChatroomBo(int seq, double[] position, UserBo userBo){
		ChatroomBo chatroom = new ChatroomBo();
		chatroom.setSeq(seq);
		chatroom.setCreateuid(userBo.getId());
		LinkedHashSet<String> userSet = chatroom.getUsers();
		userSet.add(userBo.getId());
		chatroom.setUsers(userSet);
		chatroom.setName(userBo.getUserName());
		chatroom.setMaster(userBo.getId());
		chatroom.setPosition(position);
		chatroom.setType(Constant.ROOM_FACE_2_FACE);
		return chatroom;
	}


	@RequestMapping("/factoface-add")
	@ResponseBody
	public String faceToFaceAdd(int seq, double px, double py,
								HttpServletRequest request, HttpServletResponse response) {
		return faceToFaceCreate(seq, px, py, request, response);
	}



	@RequestMapping("/trans-chatroom")
	@ResponseBody
	public String tansRoom(String chatroomid, String userid,
								HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (chatroomBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		UserBo master = userService.getUser(userid);
		if (master == null) {
			return CommonUtil.toErrorResult(ERRORCODE.USER_NULL.getIndex(),
					ERRORCODE.USER_NULL.getReason());
		}
		if (userBo.getId().equals(chatroomBo.getMaster())) {
			chatroomService.updateMaster(chatroomid, userid);
		} else {
			return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_NOT_MASTER.getIndex(),
					ERRORCODE.CIRCLE_NOT_MASTER.getReason());
		}

		// 通知群主变更通知
		JSONObject json = new JSONObject();
		json.put("masterId", master.getId());
		json.put("masterName", master.getUserName());
		String res3 = IMUtil.notifyInChatRoom(Constant.MASTER_CHANGE_CHAT_ROOM, chatroomid, json.toString());
		if(!IMUtil.FINISH.equals(res3)){
			logger.error("failed notifyInChatRoom Constant.SOME_ONE_QUIT_CHAT_ROOM, %s",res3);
			return res3;
		}

        JPushUtil.pushTo(userBo.getUserName()+"将"+chatroomBo.getName()+"转让给了您", userid);
		return Constant.COM_RESP;
	}

	@RequestMapping("/update-name")
	@ResponseBody
	public String updateName(String chatroomid, String name,
								HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (chatroomBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		if (chatroomBo.getUsers().contains(userBo.getId())) {
			chatroomService.updateName(chatroomid, name, true);
		} else {
			return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_USER_NULL.getIndex(),
					ERRORCODE.CIRCLE_USER_NULL.getReason());
		}

		// 向群中发某人修改群聊名称通知
		JSONObject json = new JSONObject();
		json.put("masterId", userBo.getId());
		json.put("masterName", userBo.getUserName());
		json.put("chatRoomName", name);
		String res2 = IMUtil.notifyInChatRoom(Constant.SOME_ONE_MODIFY_NAME_OF_CHAT_ROOM, chatroomid, json.toString());
		if(!IMUtil.FINISH.equals(res2)){
			logger.error("failed notifyInChatRoom Constant.SOME_ONE_MODIFY_NAME_OF_CHAT_ROOM, %s",res2);
		}

		return Constant.COM_RESP;
	}

	@RequestMapping("/update-description")
	@ResponseBody
	public String updateDescription(String chatroomid, String description,
							 HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (chatroomBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		if (userBo.getId().equals(chatroomBo.getMaster())) {
			chatroomService.updateDescription(chatroomid, description);
		} else {
			return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_NOT_MASTER.getIndex(),
					ERRORCODE.CIRCLE_NOT_MASTER.getReason());
		}
		return Constant.COM_RESP;
	}

	@RequestMapping("/update-open")
	@ResponseBody
	public String updateOpen(String chatroomid, boolean isOpen,
									HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (chatroomBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		if (userBo.getId().equals(chatroomBo.getMaster())) {
			chatroomService.updateOpen(chatroomid, isOpen);
		} else {
			return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_NOT_MASTER.getIndex(),
					ERRORCODE.CIRCLE_NOT_MASTER.getReason());
		}
		return Constant.COM_RESP;
	}

	@RequestMapping("/update-verify")
	@ResponseBody
	public String updateVerify(String chatroomid, boolean isVerify,
									HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (chatroomBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		if (userBo.getId().equals(chatroomBo.getMaster())) {
			chatroomService.updateVerify(chatroomid, isVerify);
		} else {
			return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_NOT_MASTER.getIndex(),
					ERRORCODE.CIRCLE_NOT_MASTER.getReason());
		}
		return Constant.COM_RESP;
	}

	@RequestMapping("/update-nickname")
	@ResponseBody
	public String updateNickname(String chatroomid, String nickname,
							   HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (chatroomBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		chatroomService.updateUserNickname(userBo.getId(),chatroomid, nickname);
		return Constant.COM_RESP;
	}

	@RequestMapping("/get-nicknames")
	@ResponseBody
	public String getNickname(String chatroomid,
								 HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (chatroomBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		List<ChatroomUserBo> chatroomUserBos = chatroomService.findByUserRoomid(chatroomid);
		List<ChatroomUserVo> userVos = new ArrayList<>();
		for (ChatroomUserBo chatroomUserBo : chatroomUserBos) {
			String userid = chatroomUserBo.getUserid();
			UserBo chatUser = userService.getUser(userid);
			if (chatUser == null) {
				chatroomService.deleteUser(chatroomUserBo.getId());
				continue;
			}
			ChatroomUserVo userVo = new ChatroomUserVo();
			if (userid.equals(chatroomBo.getMaster())) {
				userVo.setRole(2);
			}
			userVo.setUserid(userid);
			userVo.setUserPic(chatUser.getHeadPictureName());
			userVo.setNickname(chatroomUserBo.getNickname());
			userVos.add(userVo);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("chatroomUsers", userVos);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/update-shownick")
	@ResponseBody
	public String updateShowNickname(String chatroomid, boolean isShowNick,
								 HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (chatroomBo == null) {
			deleteNickname(userBo.getId(), chatroomid);
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		chatroomService.updateShowNick(userBo.getId(), chatroomid, isShowNick);
		return Constant.COM_RESP;
	}

	@RequestMapping("/update-disturb")
	@ResponseBody
	public String updateDisturb(String chatroomid, boolean isDisturb,
								 HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (chatroomBo == null) {
			deleteNickname(userBo.getId(), chatroomid);
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		ChatroomUserBo chatroomUserBo = chatroomService.findChatUserByUserAndRoomid(userBo.getId(), chatroomid);
		//单人聊天也存在免打扰信息
		if (chatroomUserBo == null) {
			chatroomUserBo = new ChatroomUserBo();
			chatroomUserBo.setChatroomid(chatroomid);
			chatroomUserBo.setUserid(userBo.getId());
			chatroomUserBo.setUsername(userBo.getUserName());
			chatroomUserBo.setShowNick(false);
			chatroomUserBo.setDisturb(isDisturb);
			chatroomService.insertUser(chatroomUserBo);
		} else {
			chatroomService.updateDisturb(chatroomUserBo.getId(), isDisturb);
		}
		return Constant.COM_RESP;
	}


	@RequestMapping("/apply-insert")
	@ResponseBody
	public String applyInsert(String chatroomid,
							 HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (chatroomBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		if (chatroomBo.isVerify()) {
			return Constant.COM_FAIL_RESP;
		} else {
			//第一个为返回结果信息，第二位term信息
			String result = IMUtil.subscribe(1,chatroomid, userBo.getId());
			if (!result.equals(IMUtil.FINISH)) {
				return result;
			}
			addChatroomUser(chatroomService, userBo, chatroomid, userBo.getUserName());
			HashSet<String> chatroom = userBo.getChatrooms();
			//个人聊天室中没有当前聊天室，则添加到个人的聊天室
			if (!chatroom.contains(chatroomid)) {
				chatroom.add(chatroomid);
				userBo.setChatrooms(chatroom);
				userService.updateChatrooms(userBo);
			}
			// 为IMUtil通知做数据准备
			LinkedHashSet<String> set = chatroomBo.getUsers();
			int size = set.size();
			if (set.contains(userBo.getId())) {
				size --;
			}
			String name = chatroomBo.getName();
			String[] tt = new String[size];
			int i=0;
			for(String uu: set){
				if (uu.equals(userBo.getId())) continue;
				tt[i++] = uu;
			}
			Object[] otherNameAndId = ChatRoomUtil.getUserNamesAndIds(userService, tt, logger);
			ArrayList<String> imNames = new ArrayList<>();
			ArrayList<String> imIds = new ArrayList<>();
			// 如果群聊没有修改过名称，自动修改名称
			if(!chatroomBo.isNameSet()){
				String newChatRoomName = ChatRoomUtil.generateChatRoomName(userService, set, chatroomid, logger);
				if(newChatRoomName != null){
					name = newChatRoomName;
					chatroomService.updateName(chatroomid, newChatRoomName, false);
				}
			}
			chatroomBo.setUsers(set);
			chatroomService.updateUsers(chatroomBo);

			addRoomInfo(userBo, chatroomid, imIds, imNames, otherNameAndId);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ret", 0);
			map.put("channelId", chatroomBo.getId());
			map.put("chatroomUser", set.size());
			map.put("chatroomName", name);
			return JSONObject.fromObject(map).toString();
		}
	}

	/**
	 * 加群验证信息提交
	 * @param chatroomid
	 * @param reason
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/add-verify")
	@ResponseBody
	public String addVerify(String chatroomid, String reason,
								HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		ReasonBo reasonBo = new ReasonBo();
		reasonBo.setStatus(0);
		reasonBo.setCreateuid(userBo.getId());
		reasonBo.setChatroomid(chatroomid);
		reasonBo.setReason(reason);
		reasonService.insert(reasonBo);
		return Constant.COM_RESP;
	}

	/**
	 * 加群验证申请信息
	 * @param chatroomid
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/apply-list")
	@ResponseBody
	public String roomVerifyList(String chatroomid, int page, int limit,
							HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		List<ReasonBo> reasonBos = reasonService.findByChatroomHis(chatroomid, page, limit);
		List<ReasonVo> reasonVos = new ArrayList<>();
		for (ReasonBo reasonBo : reasonBos) {
			UserBo user = userService.getUser(reasonBo.getCreateuid());
			if (user != null) {
				ReasonVo reasonVo = new ReasonVo();
				BeanUtils.copyProperties(user, reasonVo);
				reasonVo.setApplyid(reasonBo.getId());
				reasonVo.setApplyTime(reasonBo.getCreateTime());
				reasonVo.setUserid(user.getId());
				reasonVo.setReason(reasonBo.getReason());
				reasonVo.setRefuse(reasonBo.getRefues());
				reasonVo.setStatus(reasonBo.getStatus());
				reasonVos.add(reasonVo);
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("userApplyVos", reasonVos);
		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 加群验证操作
	 * @param chatroomid
	 * @param refues
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/apply-operate")
	@ResponseBody
	public String applyVerify(String chatroomid, String applyid, boolean isAgree, String refues,
							HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		ChatroomBo chatroomBo = chatroomService.get(chatroomid);
		if (chatroomBo == null) {
			CommonUtil.toErrorResult(ERRORCODE.CHATROOM_NULL.getIndex(),
					ERRORCODE.CHATROOM_NULL.getReason());
		}
		ReasonBo reasonBo = reasonService.findById(applyid);
		if (reasonBo == null) {
			CommonUtil.toErrorResult(ERRORCODE.CHATROOM_APPLY_NULL.getIndex(),
					ERRORCODE.CHATROOM_APPLY_NULL.getReason());
		}
		String name = chatroomBo.getName();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("channelId", chatroomBo.getId());
		if (isAgree) {
			//第一个为返回结果信息，第二位term信息
			String result = IMUtil.subscribe(1,chatroomid, userBo.getId());
			if (!result.equals(IMUtil.FINISH)) {
				return result;
			}
			addChatroomUser(chatroomService, userBo, chatroomid, userBo.getUserName());
			HashSet<String> chatroom = userBo.getChatrooms();
			//个人聊天室中没有当前聊天室，则添加到个人的聊天室
			if (!chatroom.contains(chatroomid)) {
				chatroom.add(chatroomid);
				userBo.setChatrooms(chatroom);
				userService.updateChatrooms(userBo);
			}
			// 为IMUtil通知做数据准备
			LinkedHashSet<String> set = chatroomBo.getUsers();
			int size = set.size();
			if (set.contains(userBo.getId())) {
				size --;
			}
			String[] tt = new String[size];
			int i=0;
			for(String uu: set){
				if (uu.equals(userBo.getId())) continue;
				tt[i++] = uu;
			}
			Object[] otherNameAndId = ChatRoomUtil.getUserNamesAndIds(userService, tt, logger);
			ArrayList<String> imNames = new ArrayList<>();
			ArrayList<String> imIds = new ArrayList<>();
			// 如果群聊没有修改过名称，自动修改名称

			if(!chatroomBo.isNameSet()){
				String newChatRoomName = ChatRoomUtil.generateChatRoomName(userService, set, chatroomid, logger);
				if(newChatRoomName != null){
					name = newChatRoomName;
					chatroomService.updateName(chatroomid, newChatRoomName, false);
				}
			}
			chatroomBo.setUsers(set);
			chatroomService.updateUsers(chatroomBo);

			addRoomInfo(userBo, chatroomid, imIds, imNames, otherNameAndId);
			reasonService.updateApply(applyid, 1, "");
			map.put("chatroomUser", set.size());
		} else {
			reasonService.updateApply(applyid, 2, refues);
			map.put("chatroomUser", chatroomBo.getUsers().size());
		}
		map.put("chatroomName", name);
		return JSONObject.fromObject(map).toString();
	}




	/**
	 * 删除群聊中的用户聊天昵称
	 * @param userid
	 * @param chatroomid
	 */
	@Async
	private void deleteNickname(String userid, String chatroomid){
		chatroomService.deleteChatroomUser(userid, chatroomid);
	}


	// 向群中某人被邀请加入群聊通知
	private void addRoomInfo(UserBo userBo, String chatroomid, List<String> imIds, List<String> imNames,
							 Object[] otherNameAndId){
		if(imIds.size() > 0 && otherNameAndId[0] != null){
			JSONObject json = new JSONObject();
			json.put("masterId", userBo.getId());
			json.put("masterName", userBo.getUserName());
			json.put("hitIds", imIds);
			json.put("hitNames", imNames);
			json.put("otherIds", otherNameAndId[1]);
			json.put("otherNames", otherNameAndId[0]);

			String res = IMUtil.notifyInChatRoom(Constant.SOME_ONE_BE_INVITED_OT_CHAT_ROOM, chatroomid, json.toString());
			if(!IMUtil.FINISH.equals(res)){
				logger.error("failed notifyInChatRoom Constant.SOME_ONE_BE_INVITED_OT_CHAT_ROOM, %s",res);
			}
		}
	}
}
