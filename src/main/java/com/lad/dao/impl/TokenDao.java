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

	@Override
	public void deletedTokenByTokenAndUserId(String token, String userId) {
		mongoTemplate.remove(new Query(Criteria.where("huaweiToken").is(token).and("userId").is(userId)), PushTokenBo.class);
	}

	@Override
	public WriteResult updateOtherStatus(String token, String userId) {
		Query query = new Query(Criteria.where("huaweiToken").is(token).and("userId").ne(userId));
		Update update = new Update();
		update.set("status", PushTokenBo.TOKEN_CLOSE);
		
		return mongoTemplate.updateMulti(query, update, PushTokenBo.class);
	}

	@Override
	public PushTokenBo findTokenByUserIdAndToken(String userId, String token) {
		return mongoTemplate.findOne(new Query(Criteria.where("userId").is(userId).and("huaweiToken").is(token).and("status").is(PushTokenBo.TOKEN_ENABLE)), PushTokenBo.class);
	}

	@Override
	public WriteResult closeTokenByUseridAndToken(String userId, String token) {
		Query query = new Query(Criteria.where("userId").is(userId).and("huaweiToken").is(token).and("status").is(PushTokenBo.TOKEN_ENABLE));
		Update update = new Update();
		update.set("status", PushTokenBo.TOKEN_CLOSE);
		
		return mongoTemplate.updateFirst(query, update, PushTokenBo.class);
	}

	@Override
	public PushTokenBo findTokenEnableByUserId(String alias) {
		Query query = new Query(Criteria.where("userId").is(alias).and("status").is(PushTokenBo.TOKEN_ENABLE));
		return mongoTemplate.findOne(query, PushTokenBo.class);	}

}
