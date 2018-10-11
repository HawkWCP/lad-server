package com.lad.dao;

import java.util.List;
import java.util.Map;

import com.lad.bo.RestHomeBo;
import com.lad.bo.RetiredPeopleBo;
import com.mongodb.WriteResult;

public interface IRestHomeDao extends IBaseDao {

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

	List<RetiredPeopleBo> findRecommendPeople(String area, boolean acceptOtherArea,int page,int limit);

	List<RestHomeBo> findRecommendHome(String homeArea, String wannaArea, int page, int limit);

	List<RestHomeBo> findHomeListByUid(int page, int limit);

}
