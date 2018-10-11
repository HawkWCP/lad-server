package com.lad.dao.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.lad.bo.RestHomeBo;
import com.lad.bo.RetiredPeopleBo;
import com.lad.dao.IRestHomeDao;
import com.lad.util.Constant;
import com.mongodb.WriteResult;

@Repository("restHomeDao")
public class RestHomeDaoImpl implements IRestHomeDao {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public String test(String str) {
		return mongoTemplate.toString() + "=====" + str;
	}

	@Override
	public RestHomeBo inserthome(RestHomeBo homeBo) {
		mongoTemplate.insert(homeBo);
		return homeBo;
	}

	@Override
	public RetiredPeopleBo inserthome(RetiredPeopleBo poepleBo) {
		mongoTemplate.insert(poepleBo);
		return poepleBo;
	}

	@Override
	public RestHomeBo findHomeById(String homeId) {
		Query query = new Query(Criteria.where("_id").is(homeId).and("deleted").is(0));
		return mongoTemplate.findOne(query, RestHomeBo.class);
	}

	@Override
	public WriteResult deleteHomeById(String homeId) {
		Query query = new Query(Criteria.where("_id").is(homeId).and("deleted").is(0));
		Update update = new Update();
		update.set("deleted", 1);
		return mongoTemplate.updateFirst(query, update, RestHomeBo.class);
	}

	@Override
	public RetiredPeopleBo findPeopleById(String peopleId) {
		Query query = new Query(Criteria.where("_id").is(peopleId).and("deleted").is(0));
		return mongoTemplate.findOne(query, RetiredPeopleBo.class);
	}

	@Override
	public WriteResult deletePeopleById(String peopleId) {
		Query query = new Query(Criteria.where("_id").is(peopleId).and("deleted").is(0));
		Update update = new Update();
		update.set("deleted", 1);
		return mongoTemplate.updateFirst(query, update, RetiredPeopleBo.class);
	}

	@Override
	public WriteResult updateHomeById(String id, Map<String, Object> params) {
		Query query = new Query(Criteria.where("_id").is(id).and("deleted").is(0));
		Update update = new Update();
		for (Entry<String, Object> entry : params.entrySet()) {
			update.set(entry.getKey(), entry.getValue());
		}
		return mongoTemplate.updateFirst(query, update, RestHomeBo.class);
	}

	@Override
	public WriteResult updatePeopleById(String id, Map<String, Object> params) {
		Query query = new Query(Criteria.where("_id").is(id).and("deleted").is(0));
		Update update = new Update();
		for (Entry<String, Object> entry : params.entrySet()) {
			update.set(entry.getKey(), entry.getValue());
		}
		return mongoTemplate.updateFirst(query, update, RetiredPeopleBo.class);
	}

	@Override
	public List<RetiredPeopleBo> findPeopleListByUid(String uid, int page, int limit) {
		Query query = new Query(Criteria.where("createuid").is(uid).and("deleted").is(0));
		query.with(new Sort(new Sort.Order(Direction.DESC, "_id")));
		int skip = page - 1 < 0 ? 0 : (page - 1) * limit;
		query.skip(skip);
		query.limit(limit);
		return mongoTemplate.find(query, RetiredPeopleBo.class);
	}

	@Override
	public List<RestHomeBo> findHomeListByUid(String uid, int page, int limit) {
		Query query = new Query(Criteria.where("createuid").is(uid).and("deleted").is(0));
		query.with(new Sort(new Sort.Order(Direction.DESC, "_id")));
		int skip = page - 1 < 0 ? 0 : (page - 1) * limit;
		query.skip(skip);
		query.limit(limit);
		return mongoTemplate.find(query, RestHomeBo.class);
	}

	@Override
	public List<RetiredPeopleBo> findRecommendPeople(String area, boolean acceptOtherArea, int page, int limit) {
		Criteria criteria = Criteria.where("deleted").is(0).and("wannaArea").regex(area + "-*");
		// 如果不接受异地
		if (!acceptOtherArea) {
			criteria.and("homeArea").regex(area + "-*");
		}
		Query query = new Query();
		query.addCriteria(criteria);

		query.with(new Sort(new Sort.Order(Direction.DESC, "_id")));
		int skip = page - 1 < 0 ? 0 : (page - 1) * limit;
		query.skip(skip);
		query.limit(limit);
		return mongoTemplate.find(query, RetiredPeopleBo.class);
	}

	@Override
	public List<RestHomeBo> findRecommendHome(String homeArea, String wannaArea, int page, int limit) {
		Criteria acceptOtherArea = Criteria.where("area").regex(wannaArea + "*").and("knightService")
				.in(Constant.YL_TS_ED);
		Criteria refuseOtherArea = Criteria.where("area").regex(wannaArea + "*").and("area").regex(homeArea + "*")
				.and("knightService").nin(Constant.YL_TS_ED);
		Criteria orOption = acceptOtherArea.orOperator(acceptOtherArea, refuseOtherArea);
		Criteria criteria = Criteria.where("deleted").is(0).andOperator(orOption);
		Query query = new Query(criteria);
		query.with(new Sort(new Sort.Order(Direction.DESC, "_id")));
		int skip = page - 1 < 0 ? 0 : (page - 1) * limit;
		query.skip(skip);
		query.limit(limit);
		return mongoTemplate.find(query, RestHomeBo.class);
	}

	@Override
	public List<RestHomeBo> findHomeListByUid(int page, int limit) {
		Query query = new Query(Criteria.where("deleted").is(0));
		query.with(new Sort(new Sort.Order(Direction.DESC, "_id")));
		int skip = page - 1 < 0 ? 0 : (page - 1) * limit;
		query.skip(skip);
		query.limit(limit);
		return mongoTemplate.find(query, RestHomeBo.class);
	}
}
