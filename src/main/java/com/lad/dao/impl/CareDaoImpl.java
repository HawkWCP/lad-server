package com.lad.dao.impl;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.lad.bo.CareAndPassBo;
import com.lad.bo.CareBo;
import com.lad.dao.ICareDao;
import com.lad.util.Constant;
import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;

@Repository("careAndPassDao")
public class CareDaoImpl implements ICareDao {
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	
	@Override
	public Map<String, Set<String>> findMarriageCareMap(String mainId) {
		return getCareQuery(Constant.MARRIAGE, mainId).getCareRoster();
	}


	@Override
	public Map<String, Set<String>> findSpouseCareMap(String mainId) {
		return getCareQuery(Constant.SPOUSE, mainId).getCareRoster();
	}


	@Override
	public Map<String, Set<String>> findTravelersCareMap(String mainId) {		
		return getCareQuery(Constant.TRAVELERS, mainId).getCareRoster();
	}
	
	@Override
	public Set<String> findMarriagePassList(String mainId) {
		return getPassQuery(Constant.MARRIAGE,mainId).getPassRoster();
	}


	@Override
	public Set<String> findSpousePassList(String mainId) {
		return getPassQuery(Constant.SPOUSE,mainId).getPassRoster();
	}


	@Override
	public Set<String> findTravelersPassList(String mainId) {
		return getPassQuery(Constant.TRAVELERS,mainId).getPassRoster();
	}
	
	/**
	 * 关注-儿媳
	 */
	@Override
	public CareAndPassBo findMarriageCare(String mainId) {		
		return getCareQuery(Constant.MARRIAGE, mainId);
	}

	/**
	 * 关注-老伴
	 */
	@Override
	public CareAndPassBo findSpouseCare(String mainId) {
		return getCareQuery(Constant.SPOUSE, mainId);
	}

	/**
	 * 关注-驴友
	 */
	@Override
	public CareAndPassBo findTravelersCare(String mainId) {
		return getCareQuery(Constant.TRAVELERS, mainId);
	}

	/**
	 * 黑名单-儿媳
	 */
	@Override
	public CareAndPassBo findMarriagePass(String mainId) {
		return getPassQuery(Constant.MARRIAGE, mainId);
	}

	/**
	 * 黑名单-老伴
	 */
	@Override
	public CareAndPassBo findSpousePass(String mainId) {
		return getPassQuery(Constant.SPOUSE, mainId);
	}

	/**
	 * 黑名单-驴友
	 */
	@Override
	public CareAndPassBo findTravelersPass(String mainId) {
		return getPassQuery(Constant.TRAVELERS, mainId);
	}
	
	/**
	 * 添加一条数据
	 */
	@Override
	public String insert(CareAndPassBo care) {
		mongoTemplate.insert(care);
		return care.getId();
	}
	
	/**
	 * 修改一条数据
	 * @param situation
	 * @param type
	 * @param mainId
	 * @param roster
	 */
	@Override
	public WriteResult updateCare(String situation, String mainId, Map<String, Set<String>> careRoster) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("mainId").is(mainId),Criteria.where("situation").is(situation),Criteria.where("deleted").is(Constant.ACTIVITY));
		query.addCriteria(criteria);

		Update update = new Update();
		update.set("careRoster", careRoster);
		return mongoTemplate.updateFirst(query, update, CareAndPassBo.class);
	}
	
	@Override
	public WriteResult updatePass(String situation, String mainId, Set<String> passRoster) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("mainId").is(mainId),Criteria.where("situation").is(situation),Criteria.where("deleted").is(Constant.ACTIVITY));
		query.addCriteria(criteria);
		
		Update update = new Update();
		update.set("passRoster", passRoster);
		return  mongoTemplate.updateFirst(query, update, CareAndPassBo.class);
	}
	
	
	
	// 设置根据主id查找实体的条件与过滤条件--care
	private CareAndPassBo getCareQuery(String situation,String mainId){
		BasicDBObject criteria = new BasicDBObject();
		criteria.put("mainId", mainId);
		criteria.put("deleted", Constant.ACTIVITY);
		criteria.put("situation", situation);
		
		BasicDBObject filter = new BasicDBObject();
		filter.put("_id", true);
		filter.put("careRoster", true);
		Query query = new BasicQuery(criteria,filter);
		return mongoTemplate.findOne(query, CareAndPassBo.class);
	}
	
	// 设置根据主id查找实体的条件与过滤条件--pass
	private CareAndPassBo getPassQuery(String situation,String mainId){
		BasicDBObject criteria = new BasicDBObject();
		criteria.put("mainId", mainId);
		criteria.put("deleted", Constant.ACTIVITY);
		criteria.put("situation", situation);
		
		BasicDBObject filter = new BasicDBObject();
		filter.put("_id", true);
		filter.put("passRoster", true);
		Query query = new BasicQuery(criteria,filter);
		return mongoTemplate.findOne(query, CareAndPassBo.class);
	}
	
	@Override
	public String test() {		
		return mongoTemplate.toString();
	}


	@Override
	public CareBo findCareByUidAndOid(String uid, String oid, int type) {
		return mongoTemplate.findOne(new Query(Criteria.where("uid").is(uid).and("oid").is(oid).and("objType").is(type)), CareBo.class);
	}


	@Override
	public void updateCare(CareBo careBo) {
		Query query = new Query(Criteria.where("_id").is(careBo.getId()));
		Update update = new Update();
		update.set("deleted", 0);
		update.set("careType", careBo.getCareType());
		update.set("updateTime", new Date());
		mongoTemplate.updateFirst(query, update, CareBo.class);
	}


	@Override
	public CareBo insert(CareBo careBo) {
		mongoTemplate.insert(careBo);
		return careBo;
	}
}
