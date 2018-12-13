package com.lad.dao;

import java.util.Collection;
import java.util.List;

import com.lad.bo.PushTokenBo;
import com.mongodb.WriteResult;

public interface ITokenDao extends IBaseDao {

	PushTokenBo findHuaweiTokenByUserId(String userId);
	PushTokenBo findXiaomiRegIdByUserId(String userId);

	WriteResult updateToken(PushTokenBo tokenBo);

	PushTokenBo insert(PushTokenBo tokenBo);

	List<PushTokenBo> findTokenByUserIds(Collection<String> useridSet,int type);

	void deletedTokenByTokenAndUserId(String token, String userId,int type);

	WriteResult updateOtherStatus(String token, String userId,int type);

	PushTokenBo findTokenByUserIdAndToken(String userId, String token,int type);

	WriteResult closeTokenByUseridAndToken(String userId, String token,int type);

	PushTokenBo findTokenEnableByUserId(String alias,int type);



}
