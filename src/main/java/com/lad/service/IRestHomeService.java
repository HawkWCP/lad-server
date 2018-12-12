package com.lad.service;

import java.util.List;
import java.util.Map;

import com.lad.bo.RestHomeBo;
import com.lad.bo.RetiredPeopleBo;
import com.mongodb.WriteResult;

public interface IRestHomeService {

	String test(String str);

	RestHomeBo inserthome(RestHomeBo homeBo);

	RetiredPeopleBo inserthome(RetiredPeopleBo poepleBo);

	RestHomeBo findHomeById(String homeId);

	WriteResult deleteHomeById(String homeId);

	RetiredPeopleBo findPeopleById(String peopleId);

	WriteResult deletePeopleById(String peopleId);

	WriteResult updateHomeById(String id, Map<String, Object> params);

	WriteResult updatePeopleById(String id, Map<String, Object> params);

	List<RetiredPeopleBo> findPeopleListByUid(String uid, int page, int limit);

	List<RestHomeBo> findHomeListByUid(String uid, int page, int limit);

	List<RetiredPeopleBo> findRecommendPeople(String area, boolean acceptOtherArea, int page, int limit);

	List<RestHomeBo> findRecommendHome(String homeArea, String wannaArea, int page, int limit);

	List<RestHomeBo> findHomeList(int page, int limit);

	List<RetiredPeopleBo> findPeopleByUserid(String userid);

	List<RestHomeBo> findRecommendHome(List<Map<String, String>> areaList, int page, int limit);

	List<RestHomeBo> findHomeListByUid(String uid);

	List<RetiredPeopleBo> findRecommendPeople(List<Map<String, Object>> conditionList, int page, int limit);

	List<RestHomeBo> findHomeListByKeyword(String keyword, int page, int limit);

	List<RestHomeBo> findHomeListByKeyword(String uid, String keyword, int page, int limit);

	List<RestHomeBo> findHomeList(String uid, int page, int limit);

	List<RestHomeBo> findRecommendHome(String uid, String homeArea, String wannaArea, int page, int limit);

	List<RetiredPeopleBo> findRecommendPeople(String uid, List<Map<String, Object>> conditionList, int page, int limit);

	List<RetiredPeopleBo> findRecommendPeople(String uid, String area, boolean acceptOtherArea, int page, int limit);

	List<RestHomeBo> findRecommendHome(String id, List<Map<String, String>> areaList, int page, int limit);

	List<RetiredPeopleBo> findPeopleListByPrice(String uid, List<Map<String, Object>> conditionList,String price, int page, int limit);

	void updateTransCount(String shareId, int num);

	void updateHomeHot(String homeId, int num, int type);
}
