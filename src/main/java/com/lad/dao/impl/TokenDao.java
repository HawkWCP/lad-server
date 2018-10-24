package com.lad.dao.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.lad.bo.PushTokenBo;
import com.lad.dao.ITokenDao;
import com.mongodb.WriteResult;

@Repository("tokenDao")
public class TokenDao implements ITokenDao {
	
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public PushTokenBo findTokenByUserId(String userId) {
		Query query = new Query(Criteria.where("userId").is(userId));
		return mongoTemplate.findOne(query, PushTokenBo.class);
	}

	@Override
	public WriteResult updateHuaweiToken(PushTokenBo tokenBo) {
		Query query = new Query(Criteria.where("userId").is(tokenBo.getUserId()));
		Update update = new Update();
		update.set("huaweiToken", tokenBo.getHuaweiToken());
		return mongoTemplate.updateFirst(query, update, PushTokenBo.class);
	}

	@Override
	public PushTokenBo insert(PushTokenBo tokenBo) {
		mongoTemplate.insert(tokenBo);
		return tokenBo;
	}

	@Override
	public List<PushTokenBo> findTokenByUserIds(Collection<String> useridSet) {
		return mongoTemplate.find(new Query(Criteria.where("userId").in(useridSet)), PushTokenBo.class);
	}

}
