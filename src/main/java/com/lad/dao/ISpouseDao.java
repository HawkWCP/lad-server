package com.lad.dao;

import java.util.List;
import java.util.Map;

import com.lad.bo.BaseBo;
import com.lad.bo.SpouseBaseBo;
import com.lad.bo.SpouseRequireBo;
import com.mongodb.WriteResult;

public interface ISpouseDao {
	String insert(BaseBo baseBo);

	SpouseBaseBo findBaseById(String baseId);

	SpouseRequireBo findRequireById(String baseId);

	WriteResult updateByParams(String spouseId, Map<String, Object> params, Class class1);

	List<SpouseBaseBo> getNewSpouse(String sex, int page, int limit, String uid);

	WriteResult deletePublish(String spouseId);

	SpouseBaseBo getSpouseByUserId(String uid);

	int getNum(String id);

	List<SpouseBaseBo> findListByKeyword(String keyWord, String sex, int page, int limit, Class clazz);

	List<Map> getRecommend(String uid, String baseId);

	int findPublishNum(String uid);

	WriteResult updateRequireSex(String requireId, String requireSex, Class clazz);

}
