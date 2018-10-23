package com.lad.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lad.bo.RestHomeBo;
import com.lad.bo.RetiredPeopleBo;
import com.lad.dao.IRestHomeDao;
import com.lad.service.IRestHomeService;
import com.mongodb.WriteResult;

@Service("restHomeService")
public class RestHomeServiceImpl implements IRestHomeService {

	@Autowired
	private IRestHomeDao restHomeDao;

	@Override
	public String test(String str) {
		return restHomeDao.test(str);
	}

	@Override
	public RestHomeBo inserthome(RestHomeBo homeBo) {
		return restHomeDao.inserthome(homeBo);
	}

	@Override
	public RetiredPeopleBo inserthome(RetiredPeopleBo poepleBo) {
		return restHomeDao.inserthome(poepleBo);
	}

	@Override
	public RestHomeBo findHomeById(String homeId) {
		return restHomeDao.findHomeById(homeId);
	}

	@Override
	public WriteResult deleteHomeById(String homeId) {
		return restHomeDao.deleteHomeById(homeId);
	}

	@Override
	public RetiredPeopleBo findPeopleById(String peopleId) {
		return restHomeDao.findPeopleById(peopleId);
	}

	@Override
	public WriteResult deletePeopleById(String peopleId) {
		return restHomeDao.deletePeopleById(peopleId);
	}

	@Override
	public WriteResult updateHomeById(String id, Map<String, Object> params) {
		return restHomeDao.updateHomeById(id, params);
	}

	@Override
	public WriteResult updatePeopleById(String id, Map<String, Object> params) {
		return restHomeDao.updatePeopleById(id, params);
	}

	@Override
	public List<RetiredPeopleBo> findPeopleListByUid(String uid, int page, int limit) {
		return restHomeDao.findPeopleListByUid(uid, page, limit);
	}

	@Override
	public List<RestHomeBo> findHomeListByUid(String uid, int page, int limit) {
		return restHomeDao.findHomeListByUid(uid, page, limit);
	}

	@Override
	public List<RetiredPeopleBo> findRecommendPeople(String area, boolean acceptOtherArea, int page, int limit) {
		return restHomeDao.findRecommendPeople(area, acceptOtherArea, page, limit);
	}

	@Override
	public List<RestHomeBo> findRecommendHome(String homeArea, String wannaArea, int page, int limit) {
		return restHomeDao.findRecommendHome(homeArea, wannaArea, page, limit);
	}

	@Override
	public List<RestHomeBo> findHomeList(int page, int limit) {
		return restHomeDao.findHomeList(page, limit);
	}

	@Override
	public List<RetiredPeopleBo> findPeopleByUserid(String userid) {
		return restHomeDao.findPeopleByUserid(userid);
	}

	@Override
	public List<RestHomeBo> findRecommendHome(List<Map<String, String>> areaList, int page, int limit) {
		return restHomeDao.findRecommendHome(areaList, page, limit);
	}

	@Override
	public List<RestHomeBo> findHomeListByUid(String uid) {
		return restHomeDao.findHomeListByUid(uid);
	}

	@Override
	public List<RetiredPeopleBo> findRecommendPeople(List<Map<String, Object>> conditionList, int page, int limit) {
		return restHomeDao.findRecommendPeople(conditionList, page, limit);
	}

	@Override
	public List<RestHomeBo> findHomeListByKeyword(String keyword, int page, int limit) {
		return restHomeDao.findHomeListByKeyword(keyword, page, limit);
	}

	@Override
	public List<RestHomeBo> findHomeListByKeyword(String uid, String keyword, int page, int limit) {
		return restHomeDao.findHomeListByKeyword(uid, keyword, page, limit);
	}

	@Override
	public List<RestHomeBo> findHomeList(String uid, int page, int limit) {
		return restHomeDao.findHomeList(uid, page, limit);
	}

	@Override
	public List<RestHomeBo> findRecommendHome(String uid, String homeArea, String wannaArea, int page, int limit) {
		return restHomeDao.findRecommendHome(uid, homeArea, wannaArea, page, limit);
	}

	@Override
	public List<RetiredPeopleBo> findRecommendPeople(String uid, List<Map<String, Object>> conditionList, int page,
			int limit) {
		return restHomeDao.findRecommendPeople(uid, conditionList, page, limit);
	}

	@Override
	public List<RetiredPeopleBo> findRecommendPeople(String uid, String area, boolean acceptOtherArea, int page,
			int limit) {
		return restHomeDao.findRecommendPeople(uid, area, acceptOtherArea, page, limit);
	}

	@Override
	public List<RestHomeBo> findRecommendHome(String id, List<Map<String, String>> areaList, int page, int limit) {
		return restHomeDao.findRecommendHome(id, areaList, page, limit);
	}

	@Override
	public List<RetiredPeopleBo> findPeopleListByPrice(String uid, List<Map<String, Object>> conditionList, String price, int page, int limit) {
		return restHomeDao.findPeopleListByPrice(uid, conditionList,price, page, limit);
	}

}
