package com.lad.dao.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.lad.bo.DynamicBo;
import com.lad.bo.UserVisitBo;
import com.lad.dao.IDynamicDao;
import com.lad.util.Constant;
import com.mongodb.WriteResult;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/9/23
 */
@Repository("dynamicDao")
public class DynamicDaoImpl implements IDynamicDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public DynamicBo insert(DynamicBo dynamicBo) {
        mongoTemplate.insert(dynamicBo);
        return dynamicBo;
    }

    @Override
    public WriteResult delete(String id) {
        Query query = new Query();
        query.addCriteria(new Criteria("_id").is(id));
        query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
        Update update = new Update();
        update.set("deleted",Constant.DELETED);
        return mongoTemplate.updateFirst(query, update, DynamicBo.class);
    }

    @Override
    public DynamicBo findById(String id) {
        Query query = new Query();
        query.addCriteria(new Criteria("_id").is(id));
        query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
        return mongoTemplate.findOne(query, DynamicBo.class);
    }

    @Override
    public WriteResult update(String id, int num, int type) {
        Query query = new Query();
        query.addCriteria(new Criteria("_id").is(id));
        query.addCriteria(new Criteria("deleted").is(Constant.ACTIVITY));
        Update update = new Update();
        switch (type) {
            case Constant.ONE:
                update.set("transNum", num);
            case Constant.TWO:
                update.set("commentNum", num);
            case Constant.THREE:
                update.set("thumpNum", num);
            default:
                update.set("transNum", num);
                break;
        }
        return mongoTemplate.updateFirst(query, update, DynamicBo.class);
    }

    @Override
    public DynamicBo findByMsgid(String msgid) {
        Query query = new Query();
        query.addCriteria(new Criteria("_id").is(msgid).and("deleted").is(Constant.ACTIVITY));
        return mongoTemplate.findOne(query, DynamicBo.class);
    }

    @Override
    public List<DynamicBo> findAllFriendsMsg(List<String> friendids, int page, int limit) {
        Query query = new Query();

        query.addCriteria(new Criteria("createuid").in(friendids).and("deleted").is(Constant.ACTIVITY));
        query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "_id")));
        //-1表示查询所有动态信息
        if (page != -1) {
            page = page < 1 ? 1 :page;
            query.skip((page -1)*limit);
            query.limit(limit);
        }
        return mongoTemplate.find(query, DynamicBo.class);
    }

    @Override
    public List<DynamicBo> findAFriendsMsg(String friendid, int page, int limit) {
        Query query = new Query();
        query.addCriteria(new Criteria("createuid").is(friendid).and("deleted").is(Constant.ACTIVITY));
        query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "_id")));
        if (page != -1) {
            page = page < 1 ? 1 : page;
            query.skip((page - 1) * limit);
            query.limit(limit);
        }
        return mongoTemplate.find(query, DynamicBo.class);
    }

	@Override
	public WriteResult updateReadToTure(String ownerid, Set<String> visitids) {
		Query query = new Query(Criteria.where("ownerid").is(ownerid).and("visitid").in(visitids).and("read").is(false).and("deleted").is(0));
		Update update = new Update();
		update.set("read", true);
		return mongoTemplate.updateMulti(query, update, UserVisitBo.class);
	}

	@Override
	public long findDynamicNotReadNum(String id) {
		return mongoTemplate.count(new Query(Criteria.where("unReadFrend").in(id).and("deleted").is(0)), DynamicBo.class);
	}

	@Override
	public WriteResult updateUnReadSet(String id, LinkedHashSet<String> unReadFrend) {
		Query query = new Query(Criteria.where("_id").is(id).and("deleted").is(0));
		Update update = new Update();
		update.set("unReadFrend", unReadFrend);
		return mongoTemplate.updateFirst(query, update, DynamicBo.class);
	}

	@Override
	public void updateThumpsubNum(String dynamicId, int num) {
        Query query = new Query();
        query.addCriteria(new Criteria("_id").is(dynamicId));
        Update update = new Update();
        update.inc("thumpsubNum", num);
        mongoTemplate.updateFirst(query, update, DynamicBo.class);		
	}
}
