package com.lad.dao.impl;

import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.lad.bo.HomepageBo;
import com.lad.dao.IHomepageDao;
import com.mongodb.WriteResult;

@Repository("homepageDao")
public class HomepageDaoImpl implements IHomepageDao {

	@Autowired
	private MongoTemplate mongoTemplate;

	public HomepageBo insert(HomepageBo homepageBo) {
		mongoTemplate.insert(homepageBo);
		return homepageBo;
	}

	public HomepageBo update_new_visitors_count(HomepageBo homepageBo) {
		Query query = new Query();
		query.addCriteria(new Criteria("_id").is(homepageBo.getId()));
		Update update = new Update();
		update.set("new_visitors_count", homepageBo.getNew_visitors_count());
		mongoTemplate.updateFirst(query, update, HomepageBo.class);
		return homepageBo;
	}

	public HomepageBo update_total_visitors_count(HomepageBo homepageBo) {
		Query query = new Query();
		query.addCriteria(new Criteria("_id").is(homepageBo.getId()));
		Update update = new Update();
		update.set("total_visitors_count", homepageBo.getTotal_visitors_count());
		mongoTemplate.updateFirst(query, update, HomepageBo.class);
		return homepageBo;
	}

	public HomepageBo selectByUserId(String userId) {
		Query query = new Query();
		query.addCriteria(new Criteria("owner_id").is(userId));
		query.addCriteria(new Criteria("deleted").is(0));
		return mongoTemplate.findOne(query, HomepageBo.class);
	}

	public HomepageBo update_visitor_ids(HomepageBo homepageBo) {
		Query query = new Query();
		query.addCriteria(new Criteria("_id").is(homepageBo.getId()));
		Update update = new Update();
		update.set("visitor_ids", homepageBo.getVisitor_ids());
		mongoTemplate.updateFirst(query, update, HomepageBo.class);
		return homepageBo;
	}

	@Override
	public WriteResult updateNewCount(String id, int num) {
		Query query = new Query();
		query.addCriteria(new Criteria("_id").is(id));
		Update update = new Update();
		if (num == 0) {
			update.set("new_visitors_count", num);
		} else {
			update.inc("new_visitors_count", num);
			update.inc("total_visitors_count", num);
		}
		return mongoTemplate.updateFirst(query, update, HomepageBo.class);
	}

	@Override
	public WriteResult update_not_push_set(String hid, HashSet<String> not_push_set) {
		Query query = new Query(Criteria.where("_id").is(hid).and("deleted").is(0));
		Update update = new Update();
		update.set("not_push_set", not_push_set);
		return mongoTemplate.updateFirst(query, update, HomepageBo.class);
	}

	@Override
	public WriteResult update_hide_record_set(String hid, HashSet<String> hide_record_set) {
		Query query = new Query(Criteria.where("_id").is(hid).and("deleted").is(0));
		Update update = new Update();
		update.set("hide_record_set", hide_record_set);
		return mongoTemplate.updateFirst(query, update, HomepageBo.class);
	}
}
