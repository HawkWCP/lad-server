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
	public List<RestHomeBo> findHomeListByUid(int page, int limit) {
		return restHomeDao.findHomeListByUid(page, limit);
	}

}
