package com.lad.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.lad.bo.MsgLogBo;
import com.lad.bo.UUID_ChannelsBo;
import com.lad.dao.IMsgLogDao;

@Repository("msgLogDao")
public class MsgLogDaoImpl implements IMsgLogDao {
    @Autowired
    @Qualifier("chatRoomDate")
    private MongoTemplate mongoTemplate;

	@Override
	public List<MsgLogBo> findLogByUid(String uuid) {
		return mongoTemplate.find(new Query(Criteria.where("uuid").is(uuid)), MsgLogBo.class);
	}

	@Override
	public List<MsgLogBo> findLogByChannel(String channel,int page,int limit) {
		Query query = new Query(Criteria.where("channel").is(channel));
		query.with(new Sort(new Sort.Order(Direction.DESC, "ts")));
		
		int skip = page-1<0?0:page-1;
		query.skip(skip);
		query.limit(limit);
		
		return mongoTemplate.find(query, MsgLogBo.class);
	}

	@Override
	public List<MsgLogBo> findLogByChannel(String channel, Long startTs, int page, int limit) {
		Query query = new Query(Criteria.where("channel").is(channel).and("ts").gt(startTs));
		query.with(new Sort(new Sort.Order(Direction.DESC, "ts")));
		
		int skip = page-1<0?0:page-1;
		query.skip(skip);
		query.limit(limit);
		
		return mongoTemplate.find(query, MsgLogBo.class);	}

	@Override
	public boolean checkUserInChannel(String id, String channel) {
		Query query = new Query(Criteria.where("_id").is(id).and("channels").in(channel));
		long count = mongoTemplate.count(query, UUID_ChannelsBo.class);
		System.out.println(query+"==========================="+count+"======================================");
		return count>0;
	}
}
