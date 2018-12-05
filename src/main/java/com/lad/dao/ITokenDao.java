package com.lad.dao;

import java.util.Collection;
import java.util.List;

import com.lad.bo.PushTokenBo;
import com.mongodb.WriteResult;

public interface ITokenDao extends IBaseDao {

	PushTokenBo findTokenByUserId(String userId);

	WriteResult updateHuaweiToken(PushTokenBo tokenBo);

	PushTokenBo insert(PushTokenBo tokenBo);

	List<PushTokenBo> findTokenByUserIds(Collection<String> useridSet);

	void deletedTokenByTokenAndUserId(String token, String userId);

	WriteResult updateOtherStatus(String token, String userId);

	PushTokenBo findTokenByUserIdAndToken(String userId, String token);

	WriteResult closeTokenByUseridAndToken(String userId, String token);

	PushTokenBo findTokenEnableByUserId(String alias);

}
