package com.lad.dao.impl;

import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.lad.bo.CrcularBo;
import com.lad.dao.ICrcularDao;

@Repository("crcularDao")
public class CrcularDaoImpl implements ICrcularDao{
    @Autowired
    private MongoTemplate mongoTemplate;

	@Override
	public HashSet<CrcularBo> insert(HashSet<CrcularBo> crculars) {
		mongoTemplate.insertAll(crculars);;
		return crculars;
	}

	@Override
	public List<CrcularBo> findCrcularById(String uid) {
		Query query = new Query(Criteria.where("targetuids").is(uid).and("deleted").is(0));
		query.with(new Sort(Direction.DESC,"_id"));
		return mongoTemplate.find(query, CrcularBo.class);
	}

	@Override
	public void updateStatus(HashSet<String> ids) {
		Query query = new Query(Criteria.where("_id").in(ids).and("deleted").is(0));
		Update update = new Update();
		update.set("status",1);
		mongoTemplate.updateMulti(query,update,CrcularBo.class);
	}

}
