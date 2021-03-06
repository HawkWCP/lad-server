package com.lad.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.lad.bo.CommentBo;
import com.lad.bo.RedstarBo;
import com.lad.dao.ICommentDao;
import com.lad.dao.IRedstarDao;
import com.lad.service.ICommentService;
import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;

/**
 * 功能描述： Time:2017/6/25
 */
@Service("commentService")
public class CommentService implements ICommentService {

	@Autowired
	private ICommentDao commentDao;

	@Autowired
	private IRedstarDao redstarDao;

	public RedstarBo insertRedstar(RedstarBo redstarBo) {
		return redstarDao.insert(redstarBo);
	}

	public CommentBo insert(CommentBo commentBo) {
		return commentDao.insert(commentBo);
	}

	@Override
	public List<CommentBo> selectByNoteid(String noteid, int page, int limit) {
		return commentDao.selectByNoteid(noteid, page, limit);
	}

	@Override
	public List<CommentBo> selectByParentid(String parentid) {
		return commentDao.selectByParentid(parentid);
	}

	@Override
	public CommentBo findById(String commentId) {
		return commentDao.findById(commentId);
	}

	@Override
	public WriteResult delete(String commentId) {
		return commentDao.delete(commentId);
	}

	@Override
	public List<CommentBo> selectByUser(String userid, int page, int limit) {
		return commentDao.selectByUser(userid, page, limit);
	}

	@Async
	public WriteResult updateRedWeek(int weekNo) {
		return redstarDao.updateRedWeek(weekNo);
	}

	public RedstarBo findRedstarBo(String userid, String circleid) {
		return redstarDao.findByUserAndCircle(userid, circleid);
	}

	@Override
	public WriteResult updateRedWeekByUser(String userid, int weekNo, int year) {
		return redstarDao.updateRedWeekByUser(userid, weekNo, year);
	}

	public WriteResult addRadstarCount(String userid, String circleid) {
		return redstarDao.addCommentCount(userid, circleid, 1);
	}

	@Override
	public List<CommentBo> selectCommentByType(int type, String id, int page, int limit) {
		return commentDao.selectCommentByType(type, id, page, limit);
	}

	@Override
	public long selectCommentByTypeCount(int type, String id) {
		return commentDao.selectCommentByTypeCount(type, id);
	}

	@Override
	public List<BasicDBObject> selectMyNoteReply(String userid, int page, int limit) {
		return commentDao.selectMyNoteReply(userid, page, limit);
	}

	@Override
	public WriteResult deleteByNote(String noteid) {
		return commentDao.deleteByNote(noteid);
	}

	@Override
	public List<CommentBo> selectByTargetUser(String targetid, String userid, int type) {
		return commentDao.selectByTargetUser(targetid, userid, type);
	}

	@Override
	public WriteResult updateThumpsubNum(String commentId, int num) {
		return commentDao.updateThumpsubNum(commentId, num);
	}

	@Override
	public List<CommentBo> findCommentsBySourceId(String sourceId) {
		return commentDao.findCommentsBySourceId(sourceId);
	}
}
