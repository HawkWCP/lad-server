package com.lad.dao;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import com.lad.bo.OldFriendRequireBo;
import com.lad.bo.UserBo;
import com.mongodb.WriteResult;

public interface IOldFriendDao extends IBaseDao {

	String getInitData(String id);

	String insert(OldFriendRequireBo requireBo);

	long getRequireCount(String uid);

	WriteResult deleteByRequireId(String uid, String requireId);

	OldFriendRequireBo getByRequireId(String id, String requireId);

	WriteResult updateByParams(Map<String,Object> params, String requireId);

	List<UserBo> findListByKeyword(String keyWord, int page, int limit, String uid);

	List<OldFriendRequireBo> findNewPublish(int page, int limit, String id);

	OldFriendRequireBo getByRequireId(String requireId);

	OldFriendRequireBo getRequireByCreateUid(String id);

	@SuppressWarnings("rawtypes")
	List<Map> getRecommend(OldFriendRequireBo require);

	int findPublishNum(String uid);

	AggregationResults<Document> epicQuery(OldFriendRequireBo require);


}
