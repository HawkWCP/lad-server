package com.lad.service.impl;

import com.lad.bo.CircleBo;
import com.lad.bo.ReasonBo;
import com.lad.dao.ICircleDao;
import com.lad.dao.IReasonDao;
import com.lad.service.ICircleService;
import com.mongodb.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service("circleService")
public class CircleServiceImpl implements ICircleService {

	@Autowired
	private ICircleDao circleDao;

	@Autowired
	private IReasonDao reasonDao;

	public CircleBo insert(CircleBo circleBo) {
		return circleDao.insert(circleBo);
	}

	public CircleBo selectById(String circleBoId) {
		return circleDao.selectById(circleBoId);
	}

	public List<CircleBo> selectByuserid(String userid) {
		return circleDao.selectByuserid(userid);
	}

	public WriteResult updateUsers(String circleBoId, HashSet<String> users) {
		return circleDao.updateUsers(circleBoId, users);
	}

	public WriteResult updateUsersApply(String circleBoId, HashSet<String> usersApply) {
		return circleDao.updateUsersApply(circleBoId, usersApply);
	}

	@Override
	public WriteResult updateApplyAgree(String circleBoId, HashSet<String> users, HashSet<String> usersApply) {
		return circleDao.updateApplyAgree(circleBoId, users, usersApply);
	}

	public WriteResult updateUsersRefuse(String circleBoId, HashSet<String> usersApply,
										 HashSet<String> usersRefuse) {
		return circleDao.updateUsersRefuse(circleBoId, usersApply, usersRefuse);
	}

	public WriteResult updateHeadPicture(String circleBoId, String headPicture) {
		return circleDao.updateHeadPicture(circleBoId, headPicture);
	}

	public List<CircleBo> selectByType(String tag, String sub_tag,
			String category) {
		return circleDao.selectByType(tag, sub_tag, category);
	}

	public WriteResult updateNotes(String circleBoId, HashSet<String> notes) {
		return circleDao.updateNotes(circleBoId, notes);
	}

	@Override
	public List<CircleBo> findByCreateid(String createid) {
		return circleDao.findByCreateid(createid);
	}

	@Override
	public WriteResult updateMaster(CircleBo circleBo) {
		return circleDao.updateMaster(circleBo);
	}

	@Override
	public List<CircleBo> findMyCircles(String userid, String startId, boolean gt, int limit) {
		return circleDao.findMyCircles(userid,startId,gt, limit);
	}

	@Override
	public List<CircleBo> selectUsersPre(String userid) {
		return circleDao.selectUsersPre(userid);
	}

	public ReasonBo insertApplyReason(ReasonBo reasonBo){
		return reasonDao.insert(reasonBo);
	}

	public ReasonBo findByUserAndCircle(String userid, String circleid){
		return reasonDao.findByUserAndCircle(userid, circleid);
	}

	public WriteResult updateApply(String reasonId, int status, String refuse){
		return reasonDao.updateApply(reasonId, status,refuse);
	}

}
