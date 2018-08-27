package com.lad.dao.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.lad.bo.BaseBo;
import com.lad.bo.TravelersRequireBo;
import com.lad.bo.UserBo;
import com.lad.dao.ITravelersDao;
import com.lad.util.CommonUtil;
import com.lad.util.Constant;
import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;

@Repository("travelersDao")
public class TravelersDaoImpl implements ITravelersDao {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public WriteResult deletePublish(String requireId) {
		Query query = new Query(Criteria.where("_id").is(requireId).and("deleted").is(Constant.ACTIVITY));
		Update update = new Update();
		update.set("deleted", Constant.DELETED);
		return mongoTemplate.updateFirst(query, update, TravelersRequireBo.class);
	}

	@Override
	public int findPublishNum(String id) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("createuid").is(id), Criteria.where("deleted").is(Constant.ACTIVITY));
		query.addCriteria(criteria);
		return (int) mongoTemplate.count(query, TravelersRequireBo.class);
	}

	@Override
	public TravelersRequireBo getRequireById(String requireId) {
		BasicDBObject criteria = new BasicDBObject();
		criteria.put("_id", requireId);
		criteria.put("deleted", Constant.ACTIVITY);
		BasicDBObject filter = new BasicDBObject();
		// deleted 与createTime就算这里过滤也会字在转json中重新初始化,所以
		filter.put("updateTime", false);
		filter.put("deleted", false);
		Query query = new BasicQuery(criteria, filter);
		return mongoTemplate.findOne(query, TravelersRequireBo.class);
	}

	@Override
	public List<TravelersRequireBo> getRequireList(String id) {
		BasicDBObject criteria = new BasicDBObject();
		criteria.put("createuid", id);
		criteria.put("deleted", Constant.ACTIVITY);
		BasicDBObject filter = new BasicDBObject();
		filter.put("destination", true);
		filter.put("type", true);
		filter.put("times", true);
		Query query = new BasicQuery(criteria, filter);
		query.with(new Sort(new Order(Direction.DESC, "createTime")));
		return mongoTemplate.find(query, TravelersRequireBo.class);
	}

	/**
	 * 向数据库插入一条数据
	 */
	@Override
	public String insert(BaseBo baseBo) {
		mongoTemplate.insert(baseBo);
		return baseBo.getId();
	}

	@Override
	public void test() {
		System.out.println(mongoTemplate);
	}

	@Override
	public List<TravelersRequireBo> getNewTravelers(int page, int limit, String id) {
		Query query = new Query();
		// 过滤时间 开始时间>当前时间 =没开始;结束时间>现在时间=没结束
		Criteria orcriteria = new Criteria();
		orcriteria.orOperator(Criteria.where("times.0").gte(getFirsrtDay()),
				Criteria.where("times.1").gte(getFirsrtDay()));
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("deleted").is(Constant.ACTIVITY), orcriteria);
		query.addCriteria(criteria);

		Logger logger = LoggerFactory.getLogger(getClass());
		query.skip((page - 1) * limit);
		query.limit(limit);
		query.with(new Sort(new Order(Direction.DESC, "createTime")));
		return mongoTemplate.find(query, TravelersRequireBo.class);
	}

	@Override
	public WriteResult updateByIdAndParams(String requireId, Map<String, Object> params) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(requireId), Criteria.where("deleted").is(Constant.ACTIVITY));
		query.addCriteria(criteria);

		Update update = new Update();
		for (Entry<String, Object> entity : params.entrySet()) {
			update.set(entity.getKey(), entity.getValue());
		}
		return mongoTemplate.updateFirst(query, update, TravelersRequireBo.class);
	}

	@Override
	public List<TravelersRequireBo> findListByKeyword(String keyWord, int page, int limit,
			Class<TravelersRequireBo> clazz) {

		// 过滤时间 开始时间>当前时间 =没开始;结束时间>现在时间=没结束
		Criteria orcriteria = new Criteria();
		orcriteria.orOperator(Criteria.where("times.0").gte(getFirsrtDay()),
				Criteria.where("times.1").gte(getFirsrtDay()));
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("destination").regex(".*" + keyWord + ".*"),
				Criteria.where("deleted").is(Constant.ACTIVITY), orcriteria);
		Query query = new Query();
		query.addCriteria(criteria);
		query.skip((page - 1) * limit);
		query.limit(limit);
		query.with(new Sort(new Order(Direction.DESC, "createTime")));
		return mongoTemplate.find(query, clazz);
	}

	@Override
	public List<Map> getRecommend(TravelersRequireBo require) {
		// 随机取100个实体
		// 过滤时间 开始时间>当前时间 =没开始;结束时间>现在时间=没结束
		Criteria orcriteria = new Criteria();
		orcriteria.orOperator(Criteria.where("times.0").gte(getFirsrtDay()),
				Criteria.where("times.1").gte(getFirsrtDay()));

		Date firsrtDay = getFirsrtDay();
		System.out.println(firsrtDay);
		// 过滤id和已删除数据
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("deleted").is(Constant.ACTIVITY),
				Criteria.where("createuid").ne(require.getCreateuid()),
				Criteria.where("destination").is(require.getDestination()), orcriteria);
		Query query = new Query(criteria);
		int count = (int) mongoTemplate.count(query, TravelersRequireBo.class);
		if (count < 100) {
			query.with(new Sort(Sort.Direction.DESC, "_id"));
		} else {
			Random r = new Random();
			int length = (count - 99) > 0 ? (count - 99) : 1;
			int skip = r.nextInt(length);
			query.skip(skip);
			query.limit(100);
		}
		List<TravelersRequireBo> find = mongoTemplate.find(query, TravelersRequireBo.class);

		DateFormat format = new SimpleDateFormat("yyyy-MM");
		List<Date> times = require.getTimes();
		long myStart = Long.valueOf(format.format(times.get(0)).replaceAll("-", ""));
		long myEnd = Long.valueOf(format.format(times.get(1)).replaceAll("-", ""));

		String type = "不限";
		if (require.getType() != null) {
			type = require.getType();
		}
		String sex = "不限";
		if (require.getSex() != null) {
			sex = require.getSex();
		}
		String[] age = require.getAge().replaceAll("岁", "").split("-");
		int minAgeReq = Integer.valueOf(age[0]);
		int maxAgeReq = Integer.valueOf(age[1]);

		List<String> temp = new ArrayList<>();
		List<Map> list = new ArrayList<>();

		for (TravelersRequireBo other : find) {
			if (temp.contains(other.getId())) {
				continue;
			}

			UserBo user = mongoTemplate.findOne(new Query(Criteria.where("_id").is(other.getCreateuid())),
					UserBo.class);

			// 目的地:25分,时段:25分,旅行方式:20分,性别":15,年龄:15
			int match = 100;

			Logger logger = LoggerFactory.getLogger(TravelersDaoImpl.class);
			logger.error("------------开始匹配  {当前匹配者为:" + user.getUserName() + ",初始分数为:" + match
					+ "}  -----------------------------------");

			// 时段
			List<Date> OtherTimes = other.getTimes();

			long othStart = Long.valueOf(format.format(OtherTimes.get(0)).replaceAll("-", ""));
			long othEnd = Long.valueOf(format.format(OtherTimes.get(1)).replaceAll("-", ""));
			// 交集或包含
			if (othStart > myEnd || othEnd < myStart) {
				match -= 25;
			}
			logger.error("旅行时段匹配,单项分数:25---意向时段:" + format.format(times.get(0)) + "~" + format.format(times.get(1))
					+ ",匹配者时段" + format.format(OtherTimes.get(0)) + "~" + format.format(OtherTimes.get(1)) + ",分数结算为:"
					+ match);

			// 旅行方式
			if (!"不限".equals(type) && !other.getType().equals(type)) {
				match -= 20;
			}
			logger.error("旅行方式匹配,单项分数为:20---意向方式为:" + type + ",匹配者意向为:" + other.getType() + ",结算分数为:" + match);
			// 驴友性别
			if (!"不限".equals(sex) && !user.getSex().equals(sex)) {
				match -= 15;
			}
			logger.error("驴友性别匹配,单项分数为:15---意向性别为:" + sex + ",匹配者性别为:" + other.getSex() + ",结算分数为:" + match);
			// 年龄匹配
			String bir = user.getBirthDay();
			int temp1 = 0;
			if (StringUtils.isEmpty(bir)) {
				match -= 15;
			} else {
				Calendar calendar = Calendar.getInstance();
				String[] split = bir.split("\\D+");
				calendar.set(Integer.valueOf((split[0])), Integer.valueOf((split[1])), Integer.valueOf((split[2])));
				int userAge = CommonUtil.getAge(calendar.getTime());
				temp1 = userAge;
				if (userAge > maxAgeReq || userAge < minAgeReq) {
					match -= 15;
				}
			}
			logger.error("驴友年龄匹配,单项分数为:15---意向年龄为:" + Arrays.toString(age) + ",匹配者年龄为:" + temp1 + ",结算分数为:" + match);
			logger.error("----------------------------end-------------------------------------------");
			if (match > 0) {
				temp.add(other.getId());
				Map map = new HashMap<>();
				map.put("match", match);
				map.put("result", other);
				list.add(map);
			}
		}
		return list;
	}

	private Date getFirsrtDay() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

}
