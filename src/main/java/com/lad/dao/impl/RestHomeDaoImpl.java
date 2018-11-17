package com.lad.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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
		if(params.size()>0) {
		for (Entry<String, Object> entry : params.entrySet()) {
			update.set(entry.getKey(), entry.getValue());
		}
		}else {
			update.set("_class", "com.lad.bo.RestHomeBo");
		}
		return mongoTemplate.updateFirst(query, update, RestHomeBo.class);
	}

	@Override
	public WriteResult updatePeopleById(String id, Map<String, Object> params) {
		Query query = new Query(Criteria.where("_id").is(id).and("deleted").is(0));
		Update update = new Update();
		if(params.size()>0) {
			for (Entry<String, Object> entry : params.entrySet()) {
				update.set(entry.getKey(), entry.getValue());
			}
		}else {
			update.set("_class", "com.lad.bo.RetiredPeopleBo");
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
	public List<RetiredPeopleBo> findRecommendPeople(String area, boolean acceptOtherArea, int page, int limit) {
		return findRecommendPeople(null, area, acceptOtherArea, page, limit);
	}

	@Override
	public List<RestHomeBo> findRecommendHome(String homeArea, String wannaArea, int page, int limit) {
		return findRecommendHome(null, homeArea, wannaArea, page, limit);
	}

	@Override
	public List<RetiredPeopleBo> findPeopleByUserid(String userid) {
		return mongoTemplate.find(new Query(Criteria.where("deleted").is(0).and("createuid").is(userid)),
				RetiredPeopleBo.class);
	}

	@Override
	public List<RestHomeBo> findRecommendHome(List<Map<String, String>> areaList, int page, int limit) {
		return findRecommendHome(null, areaList, page, limit);
	}

	@Override
	public List<RestHomeBo> findHomeListByUid(String uid) {
		return findHomeListByUid(uid, 0, 0);
	}

	@Override
	public List<RestHomeBo> findHomeListByUid(String uid, int page, int limit) {
		Query query = new Query(Criteria.where("createuid").is(uid).and("deleted").is(0));
		query.with(new Sort(new Sort.Order(Direction.DESC, "_id")));
		if (page != 0 && limit != 0) {
			int skip = page - 1 < 0 ? 0 : (page - 1) * limit;
			query.skip(skip);
			query.limit(limit);
		}
		return mongoTemplate.find(query, RestHomeBo.class);
	}

	@Override
	public List<RestHomeBo> findHomeList(int page, int limit) {
		return findHomeList(null, page, limit);
	}

	@Override
	public List<RetiredPeopleBo> findRecommendPeople(List<Map<String, Object>> conditionList, int page, int limit) {
		return findRecommendPeople(null, conditionList, page, limit);
	}

	@Override
	public List<RestHomeBo> findHomeListByKeyword(String keyword, int page, int limit) {
		return findHomeListByKeyword(null, keyword, page, limit);
	}

	@Override
	public List<RestHomeBo> findHomeListByKeyword(String uid, String keyword, int page, int limit) {
		Query query = new Query();
		Criteria orCriteria = new Criteria();
		orCriteria.orOperator(Criteria.where("area").regex(keyword), Criteria.where("name").regex(keyword));
		Criteria andCriteria = Criteria.where("deleted").is(0);
		if (uid != null) {
			andCriteria.and("createuid").ne(uid);
		}
		Criteria criteria = new Criteria();
		criteria.andOperator(andCriteria, orCriteria);
		query.addCriteria(criteria);
		System.out.println(query);
		query.with(new Sort(new Sort.Order(Direction.DESC, "_id")));
		int skip = page - 1 < 0 ? 0 : (page - 1) * limit;
		query.skip(skip);
		query.limit(limit);

		return mongoTemplate.find(query, RestHomeBo.class);
	}

	@Override
	public List<RestHomeBo> findHomeList(String uid, int page, int limit) {
		Query query = new Query();
		Criteria criteria = Criteria.where("deleted").is(0);
		if (StringUtils.isNotEmpty(uid)) {
			criteria.and("createuid").ne(uid);
		}
		query.addCriteria(criteria);
		query.with(new Sort(new Sort.Order(Direction.DESC, "_id")));
		int skip = page - 1 < 0 ? 0 : (page - 1) * limit;
		query.skip(skip);
		query.limit(limit);
		return mongoTemplate.find(query, RestHomeBo.class);
	}

	@Override
	public List<RestHomeBo> findRecommendHome(String uid, String homeArea, String wannaArea, int page, int limit) {
		List<Map<String, String>> areaList = new ArrayList<>();
		Map<String, String> areaMap = new HashMap<>();
		areaMap.put("wannaArea", wannaArea);
		areaMap.put("homeArea", homeArea);
		areaList.add(areaMap);
		return findRecommendHome(uid, areaList, page, limit);
	}

	@Override
	public List<RetiredPeopleBo> findRecommendPeople(String uid, List<Map<String, Object>> conditionList, int page,
			int limit) {
		return findPeopleListByPrice(uid, conditionList, null, page, limit);
	}

	@Override
	public List<RetiredPeopleBo> findRecommendPeople(String uid, String area, boolean acceptOtherArea, int page,
			int limit) {

		List<Map<String, Object>> conditionList = new ArrayList<>();
		Map<String, Object> conditionMap = new HashMap<>();
		conditionMap.put("area", area);
		conditionMap.put("acceptOtherArea", acceptOtherArea);
		conditionList.add(conditionMap);
		return findRecommendPeople(uid, conditionList, page, limit);
	}

	@Override
	public List<RestHomeBo> findRecommendHome(String id, List<Map<String, String>> areaList, int page, int limit) {
		List<RestHomeBo> result = new ArrayList<>();

		for (Map<String, String> areaMap : areaList) {
			String wannaRegex = areaMap.get("wannaArea") + ".*";
			String homeRegex = areaMap.get("homeArea") + ".*";


			
//			Criteria orCriteria = new Criteria();
//			意向地址等于所在地址,并且接受异地;
//			Criteria acceptTrueAndWannaTrue = Criteria.where("area").regex(wannaRegex).and("acceptOtherArea").is(true);
//			家庭地址等于所在地址,并且接受异地;
//			Criteria acceptTrueAndHomeTrue = Criteria.where("area").regex(homeRegex).and("acceptOtherArea").is(true);
//			家庭地址等于所在地址,不接受异地
//			Criteria acceptFalseAndHomeTrue = Criteria.where("area").regex(homeRegex).and("area").regex(wannaRegex).and("acceptOtherArea").is(false);
//
//			orCriteria.orOperator(acceptTrueAndWannaTrue, acceptFalseAndHomeTrue);
//
//			Criteria criteria = Criteria.where("deleted").is(0).andOperator(orCriteria);
			
			Criteria criteria = Criteria.where("area").regex(wannaRegex).and("deleted").is(0);
			if(!wannaRegex.equals(homeRegex)) {
				criteria.and("acceptOtherArea").is(true);
			}
			if (StringUtils.isNotEmpty(id)) {
				criteria.and("createuid").ne(id);
			}
			Query query = new Query(criteria);
			query.with(new Sort(new Sort.Order(Direction.DESC, "_id")));
			int skip = page - 1 < 0 ? 0 : (page - 1) * limit;
			query.skip(skip);
			query.limit(limit);
			List<RestHomeBo> find = mongoTemplate.find(query, RestHomeBo.class);
			Set<String> idList = new HashSet<>();
			for (RestHomeBo restHomeBo : find) {
				if (!idList.contains(restHomeBo.getId())) {
					result.add(restHomeBo);
					idList.add(restHomeBo.getId());
				}
			}
		}
		return result;
	}

	@Override
	public List<RetiredPeopleBo> findPeopleListByPrice(String uid, List<Map<String, Object>> conditionList,
			String price, int page, int limit) {
		List<RetiredPeopleBo> result = new ArrayList<>();

		for (Map<String, Object> condition : conditionList) {
			String areaRegex = condition.get("area") + ".*";
			Criteria criteria = Criteria.where("deleted").is(0).and("wannaArea").regex(areaRegex); // 如果不接受异地
			if (!(boolean) condition.get("acceptOtherArea")) {
				criteria.and("homeArea").regex(areaRegex);
			}
			if (StringUtils.isNotEmpty(uid)) {
				criteria.and("createuid").ne(uid);
			}
			if (StringUtils.isNotEmpty(price)) {
				criteria.and("price").is(price);
			}
			Query query = new Query();
			query.addCriteria(criteria);

			query.with(new Sort(new Sort.Order(Direction.DESC, "_id")));
			int skip = page - 1 < 0 ? 0 : (page - 1) * limit;
			query.skip(skip);
			query.limit(limit);

			List<RetiredPeopleBo> find = mongoTemplate.find(query, RetiredPeopleBo.class);
			Set<String> idList = new HashSet<>();
			for (RetiredPeopleBo retiredPeopleBo : find) {
				if (!idList.contains(retiredPeopleBo.getId())) {
					result.add(retiredPeopleBo);
					idList.add(retiredPeopleBo.getId());
				}
			}
		}

		return result;
	}
}
