package com.lad.dao.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSON;
import com.lad.bo.OldFriendRequireBo;
import com.lad.bo.UserBo;
import com.lad.bo.UserTasteBo;
import com.lad.dao.IOldFriendDao;
import com.lad.util.CommonUtil;
import com.lad.util.Constant;
import com.mongodb.WriteResult;

@Repository("oldFriendDao")
public class OldFriendDaoImpl implements IOldFriendDao {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public String getInitData(String id) {
		OldFriendRequireBo oldFriendRequire = mongoTemplate.findOne(
				new Query(Criteria.where("createuid").is(id).and("deleted").is(Constant.ACTIVITY)),
				OldFriendRequireBo.class);
		if (oldFriendRequire == null) {
			return null;
		}
		return oldFriendRequire.getId();
	}

	@Override
	public String insert(OldFriendRequireBo requireBo) {
		mongoTemplate.save(requireBo);
		return requireBo.getId();
	}

	@Override
	public long getRequireCount(String uid) {
		return mongoTemplate.count(new Query(Criteria.where("createuid").is(uid).and("deleted").is(Constant.ACTIVITY)),
				OldFriendRequireBo.class);
	}

	@Override
	public WriteResult deleteByRequireId(String uid, String requireId) {
		Query query = new Query(
				Criteria.where("createuid").is(uid).and("_id").is(requireId).and("deleted").is(Constant.ACTIVITY));
		Update update = new Update();
		update.set("deleted", Constant.DELETED);
		return mongoTemplate.updateFirst(query, update, OldFriendRequireBo.class);
	}

	@Override
	public OldFriendRequireBo getByRequireId(String id, String requireId) {
		return mongoTemplate.findOne(new Query(
				Criteria.where("createuid").is(id).and("_id").is(requireId).and("deleted").is(Constant.ACTIVITY)),
				OldFriendRequireBo.class);
	}

	@Override
	public WriteResult updateByParams(Map<String, Object> params, String requireId) {
		Query query = new Query(Criteria.where("_id").is(requireId).and("deleted").is(Constant.ACTIVITY));
		Update update = new Update();
		for (Entry<String, Object> entity : params.entrySet()) {
			update.set(entity.getKey(), entity.getValue());
		}
		return mongoTemplate.updateFirst(query, update, OldFriendRequireBo.class);
	}

	@Override
	public List<UserBo> findListByKeyword(String keyWord, int page, int limit, String uid) {

		Query query = new Query();
		Criteria c = new Criteria();
		c.orOperator(Criteria.where("userName").regex(".*" + keyWord + ".*"),
				Criteria.where("address").regex(".*" + keyWord + ".*"));
		Criteria criteria = new Criteria();
		criteria.andOperator(c, Criteria.where("deleted").is(Constant.ACTIVITY), Criteria.where("createuid").ne(uid));

		query.addCriteria(criteria);
		query.skip((page - 1) * limit);
		query.limit(limit);
		query.with(new Sort(new Order(Direction.DESC, "createTime")));
		return mongoTemplate.find(query, UserBo.class);
	}

	@Override
	public List<OldFriendRequireBo> findNewPublish(int page, int limit, String id) {
		Query query = new Query(Criteria.where("deleted").is(Constant.ACTIVITY));
		query.with(new Sort((new Order(Direction.DESC, "createTime"))));
		return mongoTemplate.find(query, OldFriendRequireBo.class);
	}

	@Override
	public OldFriendRequireBo getByRequireId(String requireId) {
		return mongoTemplate.findOne(
				new Query(Criteria.where("_id").is(requireId).and("deleted").is(Constant.ACTIVITY)),
				OldFriendRequireBo.class);
	}

	@Override
	public OldFriendRequireBo getRequireByCreateUid(String id) {
		return mongoTemplate.findOne(new Query(Criteria.where("createuid").is(id).and("deleted").is(Constant.ACTIVITY)),
				OldFriendRequireBo.class);
	}

	@Override
	public List<Map> getRecommend(OldFriendRequireBo require) {

		// 随机取100个实体
		Query query = new Query(
				Criteria.where("deleted").is(Constant.ACTIVITY).and("createuid").ne(require.getCreateuid()));
		int count = (int) mongoTemplate.count(query, OldFriendRequireBo.class);

		if (count < 100) {
			query.with(new Sort(Sort.Direction.DESC, "_id"));
		} else {
			Random r = new Random();
			int length = (count - 99) > 0 ? (count - 99) : 1;
			int skip = r.nextInt(length);
			query.skip(skip);
			query.limit(100);
		}

		List<OldFriendRequireBo> find = mongoTemplate.find(query, OldFriendRequireBo.class);

		// 性别要求
		String sexRequire = "不限";
		if (require.getSex() != null) {
			sexRequire = require.getSex();
		}

		// 年龄要求
		String[] reqAges = require.getAge().replaceAll("岁", "").split("-");
		int minAgeReq = Integer.valueOf(reqAges[0]);
		int maxAgeReq = Integer.valueOf(reqAges[1]);

		// 兴趣爱好
		Map<String, Set<String>> myHobbys = require.getHobbys();
		Set<String> mhSet = new LinkedHashSet<>();
		for (String key : myHobbys.keySet()) {
			mhSet.addAll(myHobbys.get(key));
		}

		// 居住地要求
		String address = "不限";
		String reqAds = require.getAddress();
		if (reqAds != null) {
			String[] addArr = reqAds.split("-");
			if (addArr.length == 1) {
				address = addArr[0];
			} else {
				address = addArr[1];
			}
		}

		List<String> temp = new ArrayList<>();
		List<Map> result = new ArrayList<>();
		for (OldFriendRequireBo bo : find) {
			if (temp.contains(bo.getId())) {
				continue;
			}

			UserBo user = mongoTemplate.findOne(new Query(Criteria.where("_id").is(bo.getCreateuid())), UserBo.class);
			if (!"不限".equals(sexRequire) && !sexRequire.equals(user.getSex())) {
				temp.add(bo.getId());
				continue;
			}
			int match = 100;
			Logger logger = LoggerFactory.getLogger(SpouseDaoImpl.class);
			logger.error("==================找老友匹配,初始分数为:100,当前参与匹配者为:" + user.getUserName() + "====================");
			// 年龄匹配
			String bir = user.getBirthDay();
			int temp1 = 0;
			if (StringUtils.isEmpty(bir)) {
				match -= 25;
			} else {
				Calendar calendar = Calendar.getInstance();
				String[] split = bir.split("\\D+");
				calendar.set(Integer.valueOf((split[0])), Integer.valueOf((split[1])), Integer.valueOf((split[2])));
				int userAge = CommonUtil.getAge(calendar.getTime());
				temp1 = userAge;
				if (userAge > maxAgeReq || userAge < minAgeReq) {
					match -= 25;
				}
			}
			logger.error("年龄匹配----意向年龄为:" + Arrays.toString(reqAges) + ",匹配者年龄为:" + temp1 + ",结算分数为:" + match);
			// 匹配居住地
			String boAdd = bo.getAddress();
			if ((boAdd == null) || (boAdd != null && !boAdd.contains(address))) {
				match -= 25;
			}
			logger.error("地址匹配:----意向地址为:" + address + ",匹配者地址为:" + boAdd + ",结算分数为:" + match);
			// 兴趣
			Map<String, Set<String>> bh = bo.getHobbys();
			int temp2 = 0;
			int mhSetLen = mhSet.size();
			if (mhSetLen > 0) {
				for (String str : mhSet) {
					for (String key : bh.keySet()) {
						if (bh.get(key).contains(str)) {
							temp2 += 1;
						}
					}
				}
				if (temp2 < mhSetLen && temp2 != 0) {
					match = match - 10 * ((mhSetLen - temp2) / (mhSetLen - 1));
				} else if (temp2 == 0) {
					match = match - 25;
				}
			}
			logger.error("兴趣匹配:----意向兴趣数量为:" + mhSetLen + ",匹配数量为:" + temp2 + ",结算分数为:" + match);
			logger.error(
					"=========================================================end==============================================================");
			if (match > 0) {
				temp.add(bo.getId());
				Map map = new HashMap<>();
				map.put("match", match);
				map.put("requireBo", bo);
				result.add(map);
			}
		}

		return result;
	}

	@Override
	public int findPublishNum(String uid) {
		return (int) mongoTemplate.count(
				new Query(Criteria.where("createuid").is(uid).and("deleted").is(Constant.ACTIVITY)),
				OldFriendRequireBo.class);
	}

	@Override
	public AggregationResults<Document> epicQuery(OldFriendRequireBo require) {
		/*
		 * db.getCollection('oldFriendRequire').aggregate([
		 * {$lookup:{from:"user",localField:"uid",foreignField:"_id",as:"user"}}
		 * , { "$unwind": "$user" }, {$match:{deleted:0,"user.sex":"女"}}])
		 */

		// 过滤条件:user性别,require状态,userId不等于require.createuid

		AggregationOperation lookup = Aggregation.lookup("user", "uid", "_id", "user");
		AggregationOperation unwind = Aggregation.unwind("user");
		AggregationOperation matches = Aggregation.match(Criteria.where("deleted").is(Constant.ACTIVITY).and("user.sex")
				.is(require.getSex()).and("createuid").ne(require.getCreateuid()));
		AggregationOperation sum = Aggregation.group("_class").count().as("sum");
		AggregationOperation sort = Aggregation.sort(Sort.Direction.DESC, "createTime");

		Aggregation aggregation = Aggregation.newAggregation(lookup, unwind, matches, sum);
		Logger logger = LoggerFactory.getLogger(OldFriendDaoImpl.class);
		logger.error("aggregate count is " + aggregation.toString() + "-----------------");
		AggregationResults<Document> count = mongoTemplate.aggregate(aggregation, "oldFriendRequire", Document.class);
		int num = 0;
		for (Document document : count) {
			num = document.getInteger("sum");
		}

		if (num < 100) {
			aggregation = Aggregation.newAggregation(lookup, unwind, matches, sort);
		} else {
			Random r = new Random();
			int length = (num - 99) > 0 ? (num - 99) : 1;
			int skipNum = r.nextInt(length);
			AggregationOperation skip = Aggregation.skip(Long.valueOf(skipNum));
			AggregationOperation limit = Aggregation.limit(100L);
			aggregation = Aggregation.newAggregation(lookup, unwind, matches, skip, limit, sort);
		}

		AggregationResults<Document> res = mongoTemplate.aggregate(aggregation, "oldFriendRequire", Document.class);
		logger.error("aggregate documents is " + aggregation.toString() + "-----------------");
		for (Document doc : res) {
			logger.error("doc is " + JSON.toJSONString(res.toString()));
			UserBo userBo = (UserBo) doc.get("user");
		}

		return res;
	}

}
