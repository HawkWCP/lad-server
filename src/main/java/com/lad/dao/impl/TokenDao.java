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
	public PushTokenBo findHuaweiTokenByUserId(String userId) {
		Query query = new Query(Criteria.where("userId").is(userId).and("type").is(1));
		return mongoTemplate.findOne(query, PushTokenBo.class);
	}
	
	@Override
	public PushTokenBo findXiaomiRegIdByUserId(String userId) {
		Query query = new Query(Criteria.where("userId").is(userId).and("type").is(2));
		return mongoTemplate.findOne(query, PushTokenBo.class);
	}
	
	
	@Override
	public WriteResult updateToken(PushTokenBo tokenBo) {
		Query query = new Query(Criteria.where("userId").is(tokenBo.getUserId()).and("type").is(tokenBo.getType()));
		Update update = new Update();
		update.set("token", tokenBo.getToken());
		update.set("status", tokenBo.getStatus());
		return mongoTemplate.updateFirst(query, update, PushTokenBo.class);
	}

	@Override
	public PushTokenBo insert(PushTokenBo tokenBo) {
		mongoTemplate.insert(tokenBo);
		return tokenBo;
	}

	@Override
	public List<PushTokenBo> findTokenByUserIds(Collection<String> useridSet,int type) {
		return mongoTemplate.find(new Query(Criteria.where("userId").in(useridSet).and("status").is(PushTokenBo.TOKEN_ENABLE).and("type").is(type)),
				PushTokenBo.class);
	}

	@Override
	public void deletedTokenByTokenAndUserId(String token, String userId,int type) {
		mongoTemplate.remove(new Query(Criteria.where("token").is(token).and("userId").is(userId).and("type").is(type)),
				PushTokenBo.class);
	}

	@Override
	public WriteResult updateOtherStatus(String token, String userId,int type) {
		Query query = new Query(Criteria.where("token").is(token).and("userId").ne(userId).and("type").is(type));
		Update update = new Update();
		update.set("status", PushTokenBo.TOKEN_CLOSE);
		return mongoTemplate.updateMulti(query, update, PushTokenBo.class);
	}

	@Override
	public PushTokenBo findTokenByUserIdAndToken(String userId, String token,int type) {
		return mongoTemplate.findOne(new Query(Criteria.where("userId").is(userId).and("token").is(token).and("type").is(type)
				.and("status").is(PushTokenBo.TOKEN_ENABLE)), PushTokenBo.class);
	}

	@Override
	public WriteResult closeTokenByUseridAndToken(String userId, String token,int type) {
		Query query = new Query(Criteria.where("userId").is(userId).and("token").is(token).and("status")
				.is(PushTokenBo.TOKEN_ENABLE).and("type").is(type));
		Update update = new Update();
		update.set("status", PushTokenBo.TOKEN_CLOSE);

		return mongoTemplate.updateFirst(query, update, PushTokenBo.class);
	}

	@Override
	public PushTokenBo findTokenEnableByUserId(String alias,int type) {
		Query query = new Query(Criteria.where("userId").is(alias).and("status").is(PushTokenBo.TOKEN_ENABLE).and("type").is(type));
		return mongoTemplate.findOne(query, PushTokenBo.class);
	}


}
