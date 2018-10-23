package com.lad.dao.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.lad.bo.ReasonBo;
import com.lad.dao.IReasonDao;
import com.lad.util.Constant;
import com.mongodb.WriteResult;

/**
 * 功能描述： Version: 1.0 Time:2017/7/5
 */
@Repository("reasonDao")
public class ReasonDaoImpl implements IReasonDao {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public ReasonBo insert(ReasonBo reasonBo) {
		mongoTemplate.insert(reasonBo);
		return reasonBo;
	}

	@Override
	public WriteResult updateApply(String id, int status, String refuse) {
		Query query = new Query();
		query.addCriteria(new Criteria("_id").is(id));
		query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
		Update update = new Update();
		update.set("status", status);
		if (status == Constant.ADD_REFUSE) {
			update.set("refuse", refuse);
		}
		update.set("updateTime", new Date());
		return mongoTemplate.updateFirst(query, update, ReasonBo.class);
	}

	@Override
	public ReasonBo findByUserAndCircle(String userid, String circleid, int status) {
		Query query = new Query();
		query.addCriteria(new Criteria("createuid").is(userid));
		query.addCriteria(new Criteria("circleid").is(circleid));
		query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
		query.addCriteria(new Criteria("status").is(status));
		return mongoTemplate.findOne(query, ReasonBo.class);
	}

	@Override
	public ReasonBo findById(String id) {
		Query query = new Query();
		query.addCriteria(new Criteria("_id").is(id));
		query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
		return mongoTemplate.findOne(query, ReasonBo.class);
	}

	@Override
	public WriteResult deleteById(String id) {
		Query query = new Query();
		query.addCriteria(new Criteria("_id").is(id));
		Update update = new Update();
		update.set("deleted", Constant.DELETED);
		return mongoTemplate.updateFirst(query, update, ReasonBo.class);
	}

	@Override
	public List<ReasonBo> findByCircle(String circleid) {
		Query query = new Query();
		query.addCriteria(new Criteria("circleid").is(circleid));
		query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
		query.addCriteria(new Criteria("status").is(Constant.ADD_APPLY));
		query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "createTime")));
		return mongoTemplate.find(query, ReasonBo.class);
	}

	public List<ReasonBo> findByChatroom(String chatroomid) {
		Query query = new Query();
		query.addCriteria(new Criteria("chatroomid").is(chatroomid));
		query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
		query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "createTime")));
		return mongoTemplate.find(query, ReasonBo.class);
	}

	public ReasonBo findByUserAndChatroom(String userid, String chatroomid) {
		Query query = new Query();
		query.addCriteria(new Criteria("createuid").is(userid));
		query.addCriteria(new Criteria("chatroomid").is(chatroomid));
		query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
		return mongoTemplate.findOne(query, ReasonBo.class);
	}

	@Override
	public List<ReasonBo> findByCircleHis(String circleid, int page, int limit) {
		Query query = new Query();
		query.addCriteria(new Criteria("circleid").is(circleid));
		query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
		query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "createTime")));
//        query.skip(page < 1 ? 1: page);
		query.skip((page - 1) * limit);
		query.limit(limit);
		return mongoTemplate.find(query, ReasonBo.class);
	}

	@Override
	public List<ReasonBo> findByChatroomHis(String chatroomid, int page, int limit) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("chatroomid").is(chatroomid),
				Criteria.where("deleted").is(Constant.ACTIVITY), Criteria.where("status").is(0));
		query.addCriteria(criteria);

		query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "createTime")));
		query.skip((page - 1) * limit);
		query.limit(limit);
		return mongoTemplate.find(query, ReasonBo.class);
	}

	@Override
	public WriteResult updateMasterApply(String id, int status, boolean isMasterApply) {
		Query query = new Query();
		query.addCriteria(new Criteria("_id").is(id));
		query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
		Update update = new Update();
		update.set("status", status);
		update.set("isMasterApply", isMasterApply);
		update.set("updateTime", new Date());
		return mongoTemplate.updateFirst(query, update, ReasonBo.class);
	}

	@Override
	public WriteResult updateUnReadNum(String userid, String circleid, int num) {
		Query query = new Query();
		query.addCriteria(new Criteria("createuid").is(userid));
		query.addCriteria(new Criteria("circleid").is(circleid));
		query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
		query.addCriteria(new Criteria("status").is(Constant.ADD_AGREE));
		Update update = new Update();
		update.inc("unReadNum", num);
		return mongoTemplate.updateFirst(query, update, ReasonBo.class);
	}

	@Override
	public WriteResult updateUnReadNumZero(String id) {
		Query query = new Query();
		query.addCriteria(new Criteria("_id").is(id));
		query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
		query.addCriteria(new Criteria("status").is(Constant.ADD_AGREE));
		Update update = new Update();
		update.set("unReadNum", 0);
		return mongoTemplate.updateFirst(query, update, ReasonBo.class);
	}

	@Override
	public WriteResult updateUnReadNumZero(String userid, String circleid) {
		Query query = new Query();
		query.addCriteria(new Criteria("createuid").is(userid));
		query.addCriteria(new Criteria("circleid").is(circleid));
		query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
		query.addCriteria(new Criteria("status").is(Constant.ADD_AGREE));
		Update update = new Update();
		update.set("unReadNum", 0);
		return mongoTemplate.updateFirst(query, update, ReasonBo.class);
	}

	@Override
	public WriteResult updateUnReadNum(HashSet<String> userids, String circleid) {
		Query query = new Query();
		query.addCriteria(new Criteria("createuid").in(userids));
		query.addCriteria(new Criteria("circleid").is(circleid));
		query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
		query.addCriteria(new Criteria("status").is(Constant.ADD_AGREE));
		Update update = new Update();
		update.inc("unReadNum", 1);
		return mongoTemplate.updateMulti(query, update, ReasonBo.class);
	}

	@Override
	public ReasonBo findByUserAdd(String userid, String circleid) {
		Query query = new Query();
		query.addCriteria(
				new Criteria("createuid").is(userid).and("circleid").is(circleid).and("deleted").is(Constant.ACTIVITY));
		return mongoTemplate.findOne(query, ReasonBo.class);
	}

	@Override
	public WriteResult removeUser(HashSet<String> removeUsers, String circleid) {
		Query query = new Query();
		query.addCriteria(new Criteria("createuid").in(removeUsers).and("circleid").is(circleid).and("deleted")
				.is(Constant.ACTIVITY));
		Update update = new Update();
		update.set("deleted", Constant.DELETED);
		return mongoTemplate.updateMulti(query, update, ReasonBo.class);
	}

	@Override
	public WriteResult removeUser(String userid, String circleid) {
		Query query = new Query();
		query.addCriteria(
				new Criteria("createuid").is(userid).and("circleid").is(circleid).and("deleted").is(Constant.ACTIVITY));
		Update update = new Update();
		update.set("deleted", Constant.DELETED);
		return mongoTemplate.updateFirst(query, update, ReasonBo.class);
	}

	@Override
	public List<ReasonBo> findByChatroom(String chatroomid, int status) {
		Query query = new Query();
		query.addCriteria(new Criteria("chatroomid").is(chatroomid));
		query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
		query.addCriteria(new Criteria("status").is(status));
		query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "_id")));
		return mongoTemplate.find(query, ReasonBo.class);
	}

	@Override
	public ReasonBo findByUserAndChatroom(String userid, String chatroomid, int status) {

		Query query = new Query();
		Criteria criteria = new Criteria("reasonType").is(1).and("createuid").is(userid).and("chatroomid")
				.is(chatroomid).and("status").is(status).and("deleted").is(Constant.ACTIVITY);
		query.addCriteria(criteria);
		return mongoTemplate.findOne(query, ReasonBo.class);
	}

	@Override
	public List<ReasonBo> findByUserAndCircle(HashSet<String> users, String circleid, int status) {
		Query query = new Query();
		query.addCriteria(new Criteria("createuid").in(users));
		query.addCriteria(new Criteria("circleid").is(circleid));
		query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
		query.addCriteria(new Criteria("status").is(status));
		return mongoTemplate.find(query, ReasonBo.class);
	}

	@Override
	public WriteResult updateUnReadSet(String userid, String circleid, HashSet<String> unReadSet) {
		Query query = new Query();
		query.addCriteria(new Criteria("createuid").is(userid));
		query.addCriteria(new Criteria("circleid").is(circleid));
		query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
		query.addCriteria(new Criteria("status").is(Constant.ADD_AGREE));
		Update update = new Update();
		update.set("unReadSet", unReadSet);
		update.set("unReadNum", unReadSet.size());
		return mongoTemplate.updateFirst(query, update, ReasonBo.class);
	}

	@Override
	public ReasonBo findByUserAndCircle(String userid, String circleid, int status, int reasonType) {
		Query query = new Query();
		query.addCriteria(new Criteria("createuid").is(userid));
		query.addCriteria(new Criteria("circleid").is(circleid));
		query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
		query.addCriteria(new Criteria("status").is(status));
		query.addCriteria(new Criteria("reasonType").is(reasonType));
		return mongoTemplate.findOne(query, ReasonBo.class);
	}
}
