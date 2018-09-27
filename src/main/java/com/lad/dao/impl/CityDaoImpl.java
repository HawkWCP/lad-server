package com.lad.dao.impl;

import com.lad.bo.CityBo;
import com.lad.dao.ICityDao;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DBObject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 功能描述： Copyright: Copyright (c) 2017 Version: 1.0 Time:2017/9/5
 */
@Repository("cityDao")
public class CityDaoImpl implements ICityDao {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<CityBo> findAllCitys() {
		return mongoTemplate.findAll(CityBo.class);
	}

	@Override
	public List<CityBo> findByParams(String province, String city, String distrit) {
		Query query = new Query();
		if (StringUtils.isNotEmpty(province)) {
			query.addCriteria(new Criteria("province").is(province));
		}
		if (StringUtils.isNotEmpty(city)) {
			query.addCriteria(new Criteria("city").is(city));
		}
		if (StringUtils.isNotEmpty(distrit)) {
			query.addCriteria(new Criteria("distrit").is(distrit));
		}
		return mongoTemplate.find(query, CityBo.class);
	}

	@Override
	public CityBo insert(CityBo cityBo) {
		mongoTemplate.insert(cityBo);
		return cityBo;
	}

	@Override
	public List<CityBo> findByParams(String province, String distrit) {
		Query query = new Query();
		if (StringUtils.isNotEmpty(province)) {
			query.addCriteria(new Criteria("province").is(province));
		}
		if (StringUtils.isNotEmpty(distrit)) {
			query.addCriteria(new Criteria("distrit").is(distrit));
		}
		return mongoTemplate.find(query, CityBo.class);
	}

	@Override
	public List<BasicDBObject> findProvince() {

		ProjectionOperation project = Aggregation.project("province");

		GroupOperation group = Aggregation.group("province").first("province").as("province");
		Aggregation aggregation = Aggregation.newAggregation(project, group,
				Aggregation.sort(new Sort(new Sort.Order(Sort.Direction.DESC, "province"))));
		AggregationResults<BasicDBObject> results = mongoTemplate.aggregate(aggregation, "city", BasicDBObject.class);
		return results.getMappedResults();
	}

	@Override
	public List<BasicDBObject> findCitys(String province) {
		Criteria criteria = new Criteria("province").is(province);
		GroupOperation group = Aggregation.group("city").first("city").as("city");
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(criteria), Aggregation.project("city"),
				group, Aggregation.sort(new Sort(new Sort.Order(Sort.Direction.DESC, "city"))));
		AggregationResults<BasicDBObject> results = mongoTemplate.aggregate(aggregation, "city", BasicDBObject.class);
		return results.getMappedResults();
	}

	@Override
	public List<String> getProvince() {
		return mongoTemplate.getCollection("city").distinct("province");
	}

	@Override
	public List<String> getCity(String provice) {
		DBObject query = new BasicDBObject();
		query.put("province", provice);
		return mongoTemplate.getCollection("city").distinct("city",query);
	}

	@Override
	public List<String> getDistrit(String city) {
		DBObject query = new BasicDBObject();
		query.put("city", city);
		return mongoTemplate.getCollection("city").distinct("distrit",query);
	}
}
