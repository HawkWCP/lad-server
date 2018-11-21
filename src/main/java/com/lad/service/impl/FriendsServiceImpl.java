package com.lad.service.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lad.bo.FriendsBo;
import com.lad.dao.IFriendsDao;
import com.lad.service.IFriendsService;
import com.mongodb.WriteResult;

@Service("friendsService")
public class FriendsServiceImpl extends BaseServiceImpl  implements IFriendsService {

	@Autowired
	private IFriendsDao friendsDao;
	
	public FriendsBo insert(FriendsBo friendsBo) {
		return friendsDao.insert(friendsBo);
	}

	public WriteResult updateBackname(String userid, String friendid, String backname,LinkedList<String> usedBackName) {
		return friendsDao.updateBackname(userid, friendid, backname,usedBackName);
	}

	public WriteResult updateTag(String userid, String friendid, List tag) {
		return friendsDao.updateTag(userid, friendid, tag);
	}

	public WriteResult updatePhone(String userid, String friendid, HashSet<String> phones) {
		return friendsDao.updatePhone(userid, friendid, phones);
	}

	public WriteResult updateDescription(String userid, String friendid, String description) {
		return friendsDao.updateDescription(userid, friendid, description);
	}

	public WriteResult updateVIP(String userid, String friendid, Integer VIP) {
		return friendsDao.updateVIP(userid, friendid, VIP);
	}

	public WriteResult updateBlack(String userid, String friendid, Integer black) {
		return friendsDao.updateBlack(userid, friendid, black);
	}

	public FriendsBo getFriendByIdAndVisitorId(String userid, String friendid) {
		return friendsDao.getFriendByIdAndVisitorId(userid, friendid);
	}
	
	public FriendsBo getFriendByIdAndVisitorIdAgree(String userid, String friendid) {
		return friendsDao.getFriendByIdAndVisitorIdAgree(userid, friendid);
	}

	// TODO
	public List<FriendsBo> getFriendByUserid(String userid) {
		List<FriendsBo> changeImgHost = changeImgHost(friendsDao.getFriendByUserid(userid));
		return changeImgHost;
	}

	public List<FriendsBo> getFriendByFriendid(String friendid) {
		return friendsDao.getFriendByFriendid(friendid);
	}

	public WriteResult delete(String userid, String friendid) {
		return friendsDao.delete(userid, friendid);
	}

	public WriteResult updateApply(String id, int apply, String chatroomid) {
		return friendsDao.updateApply(id, apply, chatroomid);
	}

	public List<FriendsBo> getApplyFriendByuserid(String userid) {
		return friendsDao.getApplyFriendByuserid(userid);
	}

	public FriendsBo get(String id) {
		return friendsDao.get(id);
	}

	@Override
	public List<FriendsBo> searchCircleUsers(HashSet<String> circleUsers, String userid, String keywords) {
		return friendsDao.searchCircleUsers(circleUsers, userid, keywords);
	}

	@Override
	public List<FriendsBo> searchInviteCircleUsers(HashSet<String> circleUsers, String userid, String keywords) {
		return friendsDao.searchInviteCircleUsers(circleUsers, userid, keywords);
	}

	@Override
	public List<FriendsBo> getFriendByUserid(String userid, Date timestap) {
		return friendsDao.getFriendByUserid(userid, timestap);
	}

	@Override
	public WriteResult updateUsernameByFriend(String friendid, String username, String userHeadPic) {
		return friendsDao.updateUsernameByFriend(friendid, username, userHeadPic);
	}

	@Override
	public List<FriendsBo> getFriendByInList(String userid, List<String> friendids) {
		return friendsDao.getFriendByInList(userid, friendids);
	}

	@Override
	public WriteResult updateRelateStatus(String id, int relateStatus, boolean isParent) {
		return friendsDao.updateRelateStatus(id, relateStatus, isParent);
	}

	@Override
	public List<FriendsBo> findAllApplyList(String userid, int page , int limit) {
		return friendsDao.findAllApplyList(userid, page, limit);
	}


	@Override
	public List<FriendsBo> findByStatus(String userid, int applyStatus, int relateStatus, int page, int limit) {
		return friendsDao.findByStatus(userid, applyStatus, relateStatus, page, limit);
	}
}
