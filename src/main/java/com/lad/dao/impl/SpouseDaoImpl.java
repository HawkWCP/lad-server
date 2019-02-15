package com.lad.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
import com.lad.bo.CareAndPassBo;
import com.lad.bo.SpouseBaseBo;
import com.lad.bo.SpouseRequireBo;
import com.lad.dao.ISpouseDao;
import com.lad.util.CommonUtil;
import com.lad.util.Constant;
import com.lad.vo.SpouseBaseVo;
import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;

@Repository("spouseDao")
@SuppressWarnings("all")
public class SpouseDaoImpl implements ISpouseDao {
	@Autowired
	private MongoTemplate mongoTemplate;

	/**
	 * 查看当前用户下的发布
	 */
	@Override
	public SpouseBaseBo getSpouseByUserId(String uid) {

		BasicDBObject criteria = new BasicDBObject();
		criteria.put("createuid", uid);
		criteria.put("deleted", Constant.ACTIVITY);
		BasicDBObject filter = new BasicDBObject();
		filter.put("pass", false);
		filter.put("care", false);
		filter.put("createuid", false);
		filter.put("updateTime", false);
		Query query = new BasicQuery(criteria, filter);
		return mongoTemplate.findOne(query, SpouseBaseBo.class);
	}

	@Override
	public WriteResult deletePublish(String spouseId) {
		Query query = new Query(Criteria.where("id").is(spouseId));
		Update update = new Update();
		update.set("deleted", 1);
		WriteResult updateFirst = mongoTemplate.updateFirst(query, update, SpouseBaseBo.class);
		return updateFirst;
	}

	@Override
	public List<SpouseBaseBo> getNewSpouse(String sex, int page, int limit, String uid) {

		Query query = new Query();
		Criteria criteria = new Criteria();

		if (sex != null) {
			criteria.andOperator(Criteria.where("deleted").is(Constant.ACTIVITY), Criteria.where("sex").is(sex));
		} else {
			criteria.andOperator(Criteria.where("deleted").is(Constant.ACTIVITY));
		}

		query.addCriteria(criteria);
		query.skip((page - 1) * limit);
		query.limit(limit);
		query.with(new Sort(new Order(Direction.DESC, "createTime")));
		return mongoTemplate.find(query, SpouseBaseBo.class);
	}

	@Override
	public String insert(BaseBo baseBo) {
		mongoTemplate.insert(baseBo);
		return baseBo.getId();
	}

	@Override
	public SpouseBaseBo findBaseById(String baseId) {
		return mongoTemplate.findOne(new Query(Criteria.where("_id").is(baseId).and("deleted").is(Constant.ACTIVITY)),
				SpouseBaseBo.class);
	}

	@Override
	public SpouseRequireBo findRequireById(String baseId) {
		return mongoTemplate.findOne(
				new Query(Criteria.where("baseId").is(baseId).and("deleted").is(Constant.ACTIVITY)),
				SpouseRequireBo.class);
	}


	@Override
	public WriteResult updateByParams(String spouseId, Map<String, Object> params, Class class1) {
		Query query = new Query();
		Criteria criteria = Criteria.where("_id").is(spouseId);
		query.addCriteria(criteria);
		Update update = new Update();
		if (params != null) {
			Set<Map.Entry<String, Object>> entrys = params.entrySet();
			for (Map.Entry<String, Object> entry : entrys) {
				update.set(entry.getKey(), entry.getValue());
			}
		}
		return mongoTemplate.updateFirst(query, update, class1);
	}

	@Override
	public int getNum(String id) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("createuid").is(id), Criteria.where("deleted").is(Constant.ACTIVITY));
		query.addCriteria(criteria);
		return (int) mongoTemplate.count(query, SpouseBaseBo.class);
	}

	@Override
	public List<SpouseBaseBo> findListByKeyword(String keyWord, String sex, int page, int limit, Class clazz) {
		Criteria c = new Criteria();
		c.orOperator(Criteria.where("nickName").regex(".*" + keyWord + ".*"),
				Criteria.where("address").regex(".*" + keyWord + ".*"));
		Criteria criertia = new Criteria();
		if (sex != null) {
			criertia.andOperator(Criteria.where("sex").is(sex), Criteria.where("deleted").is(Constant.ACTIVITY), c);
		} else {
			criertia.andOperator(Criteria.where("deleted").is(Constant.ACTIVITY), c);
		}

		Query query = new Query();
		query.addCriteria(criertia).skip((page - 1) * limit).limit(limit)
				.with(new Sort(new Order(Direction.DESC, "createTime")));
		return mongoTemplate.find(query, clazz);
	}

	@Override
	public List<Map> getRecommend(String uid, String baseId) {
		// 查询关于与黑名单
		CareAndPassBo careAndPass = mongoTemplate.findOne(new Query(Criteria.where("mainId").is(baseId)),
				CareAndPassBo.class);
		Set<String> skipId = new LinkedHashSet<>();
		if (careAndPass != null) {
			// 将黑名单加入跳过列表
			Set<String> passRoster = careAndPass.getPassRoster();
			if (passRoster != null) {
				skipId.addAll(passRoster);
			}
			Map<String, Set<String>> careRoster = careAndPass.getCareRoster();
			if (careRoster != null) {
				for (String key : careRoster.keySet()) {
					skipId.addAll(careRoster.get(key));
				}
			}
		}
		/*=============以下用与打日志,可删================*/
		SpouseBaseBo baseBo = mongoTemplate.findOne(new Query(Criteria.where("_id").is(baseId).and("deleted").is(Constant.ACTIVITY)), SpouseBaseBo.class);
		/*=============================================*/
		
		SpouseRequireBo require = mongoTemplate.findOne(
				new Query(Criteria.where("baseId").is(baseId).and("deleted").is(Constant.ACTIVITY)),
				SpouseRequireBo.class);
		// 随机取100个实体
		Query query = new Query(Criteria.where("sex").is(require.getSex()).and("deleted").is(Constant.ACTIVITY)
				.and("createuid").ne(uid).and("_id").nin(skipId));
		int count = (int) mongoTemplate.count(query, SpouseBaseBo.class);
		if (count < 100) {
			query.with(new Sort(Sort.Direction.DESC, "_id"));
		} else {
			Random r = new Random();
			int length = (count - 99) > 0 ? (count - 99) : 1;
			int skip = r.nextInt(length);
			query.skip(skip);
			query.limit(100);
		}

		List<SpouseBaseBo> find = mongoTemplate.find(query, SpouseBaseBo.class);

		String regex = "\\D+";
		// 年龄要求
		int minAge = 17;
		int maxAge = 100;
		String reqAge = require.getAge();
		if (reqAge != null) {
			String[] age = reqAge.split("-");
			minAge = Integer.valueOf(age[0].replaceAll(regex, ""));
			maxAge = Integer.valueOf(age[1].replaceAll(regex, ""));
		}

		// 月收入要求
		int minSal = 0;
		int maxSal = 30000;
		String reqSal = require.getSalary();
		if (reqSal != null) {
			String[] salary = reqSal.split("-");
			minSal = Integer.valueOf(salary[0].replaceAll(regex, ""));
			maxSal = Integer.valueOf(salary[1].replaceAll(regex, ""));
		}

		// 居住地 同省:50分 同市80分 同县100
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

		// 兴趣爱好
		Map<String, Set<String>> myHobbys = require.getHobbys();
		Set<String> mhSet = new LinkedHashSet<>();
		for (String key : myHobbys.keySet()) {
			mhSet.addAll(myHobbys.get(key));
		}
		
		
		List<Map> list = new ArrayList<>();
		List tempList = new ArrayList<>();
		for (SpouseBaseBo bo : find) {
			if (tempList.contains(bo.getId())) {
				continue;
			}
			
			Logger logger = LoggerFactory.getLogger(SpouseDaoImpl.class);
			logger.info("==================找老伴匹配,初始分数为:100,要求者为:"+baseBo.getNickName()+",当前参与匹配者为:"+bo.getNickName()+"====================");
			
			int match = 100;
			// 地址匹配
			String boAdd = bo.getAddress();
			if((boAdd == null) || (boAdd!=null && !boAdd.contains(address))){
				match -= 25;
			}
			logger.info("地址匹配:----意向地址为:"+address+",匹配者地址为:"+boAdd+",结算分数为:"+match);
			
			// 年龄
			int boAge = bo.getAge();
			int dif = 0;
			if(boAge<minAge){
				dif = (minAge-boAge)*5<25?(minAge-boAge)*5:25;
			}else if(boAge>maxAge){
				dif = (boAge-maxAge)*5<25?(boAge-maxAge)*5:25;
			}
			match = match - dif;
			
			logger.info("年龄匹配:----意向年龄为:"+reqAge+",匹配者年龄为:"+boAge+",结算分数为:"+match);
			// 兴趣
			Map<String, Set<String>> bh = bo.getHobbys();
			int temp = 0;
			int mhSetLen =mhSet.size();
			if(mhSetLen>0){
				for (String str : mhSet) {
					for (String key : bh.keySet()) {
						if(bh.get(key).contains(str)){
							temp+=1;
						}
					}
				}
				if(temp < mhSetLen && temp!=0){
					match = match-10*((mhSetLen-temp)/(mhSetLen-1));
				}else if(temp==0 ){
					match = match- 25;
				} 
			}
			
			logger.info("兴趣匹配:----意向兴趣数量为:"+mhSetLen+",匹配数量为:"+temp+",结算分数为:"+match);


			// 月收入
			String[] bsArr = bo.getSalary().split("-");
			int minBs = Integer.valueOf(bsArr[0].replaceAll(regex, ""));
			int maxBs = Integer.valueOf(bsArr[1].replaceAll(regex, ""));
			if(maxBs<minSal){
				match = match-25; 
			}
			logger.info("收入匹配:----意向收入与为:"+reqSal+",匹配者收入为:"+bo.getSalary()+",结算分数为:"+match);
			logger.info("=========================================================end==============================================================");
			
			
			tempList.add(bo.getId());
			if (match >= 60) {
				Map map = new HashMap<>();
				map.put("match", match);
				SpouseBaseVo baseVo = new SpouseBaseVo();
				BeanUtils.copyProperties(bo, baseVo);
				baseVo = (SpouseBaseVo) CommonUtil.vo_format(baseVo, SpouseBaseVo.class);
				map.put("spouseBo", baseVo);
				list.add(map);
			}
		}

		return list;
	}

	@Override
	public int findPublishNum(String uid) {
		return (int) mongoTemplate.count(
				new Query(Criteria.where("createuid").is(uid).and("deleted").is(Constant.ACTIVITY)),
				SpouseBaseBo.class);
	}

	@Override
	public WriteResult updateRequireSex(String requireId, String requireSex, Class clazz) {
		Update update = new Update();
		update.set("sex", requireSex);
		Query query = new Query(Criteria.where("baseId").is(requireId));
		return mongoTemplate.updateFirst(query, update, clazz);
	}

}
