package com.lad.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.lad.bo.PushTokenBo;
import com.mongodb.WriteResult;

public interface ITokenService {

	PushTokenBo findTokenByUserId(String userId);

	WriteResult updateHuaweiToken(PushTokenBo tokenBo);

	PushTokenBo insert(PushTokenBo tokenBo);

	List<PushTokenBo> findTokenByUserIds(Collection<String> useridSet);

	void deletedTokenByTokenAndUserId(String token, String userId);

}
