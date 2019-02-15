package com.lad.controller;

import java.util.*;

import com.lad.bo.*;
import org.apache.commons.lang3.StringUtils;
import org.redisson.misc.Hash;
import org.springframework.beans.BeanUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lad.constants.UserCenterConstants;
import com.lad.vo.ChatroomUserVo;
import com.lad.vo.ChatroomVo;
import com.lad.vo.CommentVo;
import com.lad.vo.CrcularVo;
import com.lad.vo.DynamicVo;
import com.lad.vo.ThumbsupBaseVo;
import com.lad.vo.UserBaseVo;

/**
 * 	1. 将controller的可复用的私有方法整合到此处,减少冗余代码
 *  2.分担BaseController功能,防止BaseContorller过于臃肿
 * @data 2019/01/08
 * @author xiangshi
 *
 */
public class ExtraController extends BaseContorller {
	
	protected List<CrcularVo> pull(String uid){
		List<CrcularBo> result = crcularService.findCrcularById(uid);
		List<CrcularVo> res = new ArrayList<>();
        HashSet<String> updateids = new HashSet<>();
		for (CrcularBo crcularBo : result) {
			CrcularVo vo = new CrcularVo();
			BeanUtils.copyProperties(crcularBo, vo);
			vo.setIsread(crcularBo.getStatus() == 1);
			res.add(vo);
            updateids.add(crcularBo.getId());
		}
        crcularService.updateStatus(updateids);
		return res;
	}
	protected void addCrcular(Collection<String>  targetuids,String title,String content,String path){
		// 通知
        HashSet<CrcularBo> insertMany = new HashSet<>();
        for(String targetId:targetuids){
            CrcularBo crcular = new CrcularBo();
            crcular.setTitle(title);
            crcular.setContent(content);
            crcular.setTargetuids(targetId);
            crcular.setStatus(0);
            crcular.setPath(path);
            insertMany.add(crcular);
        }

		crcularService.insert(insertMany);
	}
	
	// 添加关注
	protected int addCare(Object mainObj,Object obj,int careType) {
		
		int type = getCareType(obj);
		if(type == -1) {
			return type;
		}
		JSONObject  mobjJson = JSON.parseObject(JSON.toJSONString(mainObj));
		String uid = mobjJson.getString("id");
		String createuid = mobjJson.getString("createuid");
		
		JSONObject objJson = JSON.parseObject(JSON.toJSONString(obj));
		String oId = objJson.getString("id");
		
		CareBo careBo = careService.findCareByUidAndOidIngoreDel(uid,oId,type);
		if(careBo!=null) {
			careBo.setDeleted(0);
			careBo.setCareType(careType);
			careService.updateCare(careBo);
		}else{
			careBo = new CareBo();
			careBo.setCareType(careType);
			careBo.setCreateuid(createuid);
			careBo.setObjType(type);
			careBo.setUid(uid);
			careBo.setOid(oId);
			careBo.setUpdateTime(new Date());
			careBo = careService.insert(careBo);
		}	
		
		return 0;
	}
		
	// 添加关注
	protected int addCare(UserBo userBo,Object obj,int careType) {
		int type = getCareType(obj);
		if(type == -1) {
			return type;
		}
		String uid = userBo.getId();
		JSONObject jsonObj = JSON.parseObject(JSON.toJSONString(obj));
		String oId = jsonObj.getString("id");
		
		CareBo careBo = careService.findCareByUidAndOidIngoreDel(uid,oId,type);
		if(careBo!=null) {
			careBo.setDeleted(0);
			careBo.setCareType(careType);
			careService.updateCare(careBo);
		}else{
			careBo = new CareBo();
			careBo.setCareType(careType);
			careBo.setCreateuid(userBo.getId());
			careBo.setObjType(type);
			careBo.setUid(uid);
			careBo.setOid(oId);
			careBo.setUpdateTime(new Date());
			careBo = careService.insert(careBo);
		}	
		return 0;
	}
	
	private int getCareType(Object obj) {
		int res = -1;
		
		String className = obj.getClass().getName();
		switch(className){
			case "com.lad.bo.RestHomeBo":
				res = 3;
				break;
			case "com.lad.bo.RetiredPeopleBo":
				res = 6;
				break;
			default:
				res = -1;
				break;
		}
		
		return res;
	}
	
	/**
	 * 
	 *	 动态模块,将Bo类型bean转换为Vo类型bean
	 * @param msgBos
	 * @param dynamicVos
	 * @param userBo
	 */
	protected void dynamicBo2vo(List<DynamicBo> msgBos, List<DynamicVo> dynamicVos, UserBo userBo) {
		// userBo 动态的浏览者
		for (DynamicBo msgBo : msgBos) {
			if(!userBo.getId().equals(msgBo.getCreateuid())) {
				// 预先判断,防止在调用重载方法是执行return导致的vo实体为空现象
				if (msgBo.getAccess_level() == UserCenterConstants.ACCESS_SECURITY_ALLOW_NONE) {
					continue;
				}
				if (msgBo.getAccess_level() == UserCenterConstants.ACCESS_SECURITY_ALLOW_PART) {
					LinkedHashSet<String> access_allow_set = msgBo.getAccess_allow_set();
					if (!access_allow_set.contains(userBo.getId())) {
						continue;
					}
				}
			}
			
			DynamicVo dynamicVo = new DynamicVo();
			
			dynamicBo2vo(msgBo, dynamicVo, userBo);
			
			dynamicVos.add(dynamicVo);
		}
	}
	
	/**
	 * 	动态模块,将Bo类型bean转换为Vo类型bean
	 * 	重载,对单个实体进行移植
	 * @param msgBo
	 * @param dynamicVo
	 * @param userBo
	 */
	protected void dynamicBo2vo(DynamicBo msgBo, DynamicVo dynamicVo, UserBo userBo) {
		System.out.println("msgBo:"+JSON.toJSONString(msgBo));

		if(!userBo.getId().equals(msgBo.getCreateuid())) {
			// 如果动态不是自己发布的,则需要验证权限
			if (msgBo.getAccess_level() == UserCenterConstants.ACCESS_SECURITY_ALLOW_NONE) {
				// 动态为私有,在此处跳出
				return;
			}
			if (msgBo.getAccess_level() == UserCenterConstants.ACCESS_SECURITY_ALLOW_PART) {
				// 动态对部分人开放,在此处退出
				LinkedHashSet<String> access_allow_set = msgBo.getAccess_allow_set();
				if (!access_allow_set.contains(userBo.getId())) {
					return;
				}
			}
				
		}
		UserBo creator = (userBo.getId().equals(msgBo.getCreateuid()))?userBo:userService.getUser(msgBo.getCreateuid());
		if(creator == null) {
			// 如果动态的创建者为空,则该条动态没有意义
			return;
		}
		BeanUtils.copyProperties(msgBo, dynamicVo);

		dynamicVo.setUserPic(creator.getHeadPictureName());
		dynamicVo.setUserid(creator.getId());
		dynamicVo.setUserName(creator.getUserName());
		if(creator!=userBo) {
			// 如果不是创建者则需要判断是否启用昵称
			FriendsBo friendsBo = friendsService.getFriendByIdAndVisitorIdAgree(userBo.getId(), creator.getId());
			if (friendsBo != null && !StringUtils.isEmpty(friendsBo.getBackname())) {
				dynamicVo.setUserName(friendsBo.getBackname());
			}
		}
		dynamicVo.setThumbsupUser(getThumber(msgBo));		
		List<CommentBo> bos = commentService.findCommentsBySourceId(msgBo.getId());
		List<CommentVo> cvos = comentBo2Vo(bos,msgBo.getId());
		dynamicVo.setComment(cvos);

		LinkedHashSet<String> atIds = msgBo.getAtIds();
		if(atIds!=null) {
			List<String> userIds = new ArrayList<>(atIds);
			List<UserBo> atUsers = userService.findUserByIds(userIds);
			ArrayList<UserBaseVo> userVos = userBo2Vo(userBo, atUsers);
			dynamicVo.setAtUsers(userVos);
		}
		dynamicVo.setTime(msgBo.getCreateTime());
		dynamicVo.setIsMyThumbsup(
				thumbsupService.findHaveOwenidAndVisitorid(msgBo.getId(), userBo.getId()) != null);

		LinkedHashSet<String> unReadFrend = msgBo.getUnReadFrend();
		if (unReadFrend != null && unReadFrend.contains(userBo.getId())) {
			unReadFrend.remove(userBo.getId());
			dynamicService.updateUnReadSet(msgBo.getId(), unReadFrend);
			dynamicVo.setNeww(true);
		}
	}

	/**
	 *  获取点赞者信息
	 * @param msgBo
	 * @return
	 */
	private List<ThumbsupBaseVo> getThumber(BaseBo msgBo) {
		// 获取所有对该信息点赞的点赞实体
		List<ThumbsupBo> tbos = thumbsupService.findThumbsupsByOwnerAndType(msgBo.getId(),4);
		// 提取操作者id
		List<String> thumbSupUids = new ArrayList<>();
		tbos.forEach(tbo -> thumbSupUids.add(tbo.getVisitor_id()));
		// 获取所有操作者实体列表
		List<UserBo> users = userService.findUserByIds(thumbSupUids);
		// 重新封装返回实体
		List<ThumbsupBaseVo> thumbsupUser = new ArrayList<>();
		for (UserBo user : users) {
			ThumbsupBaseVo tbvo = new ThumbsupBaseVo();
			tbvo.setUserName(user.getUserName());
			tbvo.setUserid(user.getId());
			String headPicture = user.getHeadPictureName();
			if(headPicture!=null) {
				tbvo.setHeadPicture(headPicture);				
			}
			thumbsupUser.add(tbvo);
		}
		// 排序
		Collections.sort(thumbsupUser, (tbvo1,tbvo2)->{
			int s1 = thumbSupUids.indexOf(tbvo1.getUserid());
			int s2 = thumbSupUids.indexOf(tbvo2.getUserid());
			return s1-s2;
		});
		return thumbsupUser;
	}
	
	/**
	 *  将List<UserBo>对象转换为List<UserBaseVo>对象
	 * @param userBo
	 * @param atUsers
	 * @return
	 */
	protected ArrayList<UserBaseVo> userBo2Vo(UserBo userBo, List<UserBo> ubos) {
		ArrayList<UserBaseVo> uvos = new ArrayList<>();
		for (UserBo user : ubos) {
			UserBaseVo userVo = new UserBaseVo();
			BeanUtils.copyProperties(user, userVo);
			FriendsBo friend = friendsService.getFriendByIdAndVisitorIdAgree(userBo.getId(), user.getId());
			userVo.setBackName(user.getUserName());
			if(friend!=null) {
				String backname = friend.getBackname();
				if(backname!=null) {
					userVo.setBackName(friend.getBackname());
				}
			}
//			userVo.setSex(user.getSex());
			// 可能为空,设置默认出生日期为1970年9月20日
//			userVo.setBirthDay(user.getBirthDay()==null?"1970年09月20日":user.getBirthDay());
			uvos.add(userVo);
		}
		return uvos;
	}
	
/*	
 * 这个方法有问题
	protected void bo2vo(ChatroomBo chatroomBo, ChatroomVo vo) {
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
			if (StringUtils.isNotEmpty(chatroomUser.getNickname())) {
				userVo.setNickname(chatroomUser.getNickname());
			} else {
				userVo.setNickname(chatUser.getUserName());
			}
			userVos.add(userVo);
		}
	}*/

	// 常用
	protected void chatroomBo2Vo(ChatroomBo chatroomBo, ChatroomVo chatroomVo) {
		BeanUtils.copyProperties(chatroomBo, chatroomVo);
		LinkedHashSet<ChatroomUserVo> userVos = chatroomVo.getUserVos();
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
			if (StringUtils.isNotEmpty(chatroomUser.getNickname())) {
				userVo.setNickname(chatroomUser.getNickname());
			} else {
				userVo.setNickname(chatUser.getUserName());
			}
			userVos.add(userVo);
		}
		chatroomVo.setUserNum(userVos.size());
		chatroomVo.setUserVos(userVos);
	}
	
	/**
	 *	 评论转换
	 * @param commentBo
	 * @return
	 */
	protected CommentVo comentBo2Vo(CommentBo commentBo,String dyid) {
		CommentVo commentVo = new CommentVo();
		BeanUtils.copyProperties(commentBo, commentVo);
		// I 的大小写对不上
		commentVo.setTargetId(commentBo.getTargetid());
		commentVo.setCommentId(commentBo.getId());
		if(dyid!=null && commentBo.getTargetid().equals(dyid)) {
			commentVo.setTargetId("");
		}
		commentVo.setUserid(commentBo.getCreateuid());
		return commentVo;
	}
	
	/**
	 *	 评论转换
	 * @param commentBo
	 * @return
	 */
	protected CommentVo comentBo2Vo(CommentBo commentBo) {
		return comentBo2Vo(commentBo,null);
	}
	
	/**
	 * 	评论转换
	 * @param lst
	 * @return
	 */
	protected List<CommentVo> comentBo2Vo(List<CommentBo> lst,String dyid){
		List<CommentVo> res = new ArrayList<>();
		Map<String,String> idMappingName = new HashMap<>();
		Map<String,String> idMappinguid = new HashMap<>();
		for (CommentBo cbo : lst) {
			CommentVo cvo = comentBo2Vo(cbo,dyid);
			// 评论id与评论者姓名
			idMappingName.put(cvo.getId(), cvo.getUserName());
			// 评论id与评论者id
			idMappinguid.put(cvo.getId(), cvo.getUserid());
			res.add(cvo);
		}
		
		res.parallelStream().filter(vo->StringUtils.isNotEmpty(vo.getTargetId())).forEach(vo->{
			vo.setParentUserName(idMappingName.get(vo.getTargetId()));
			vo.setParentUserid(idMappinguid.get(vo.getTargetId()));
		});
		
		return res;
		
	}
}
