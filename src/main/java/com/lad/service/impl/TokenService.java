package com.lad.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lad.bo.PushTokenBo;
import com.lad.dao.ITokenDao;
import com.lad.service.ITokenService;
import com.mongodb.WriteResult;

@Service("tokenService")
public class TokenService implements ITokenService {

	@Autowired
	private ITokenDao tokenDao;

	@Override
	public PushTokenBo findTokenByUserId(String userId) {
		return tokenDao.findTokenByUserId(userId);
	}

	@Override
	public WriteResult updateHuaweiToken(PushTokenBo tokenBo) {
		return tokenDao.updateHuaweiToken(tokenBo);
	}

	@Override
	public PushTokenBo insert(PushTokenBo tokenBo) {
		return tokenDao.insert(tokenBo);
	}

	@Override
	public List<PushTokenBo> findTokenByUserIds(Collection<String> useridSet) {
		return tokenDao.findTokenByUserIds(useridSet);
	}

	@Override
	public void deletedTokenByTokenAndUserId(String token, String userId) {
		tokenDao.deletedTokenByTokenAndUserId(token, userId);
	}

	@Override
	public WriteResult updateOtherStatus(String token, String userId) {
		return tokenDao.updateOtherStatus( token,  userId);
	}

	@Override
	public PushTokenBo findTokenByUserIdAndToken(String userId, String token) {
		return tokenDao.findTokenByUserIdAndToken( userId,  token);
	}

	@Override
	public WriteResult closeTokenByUseridAndToken(String userId, String token) {
		return tokenDao.closeTokenByUseridAndToken( userId,  token);
	}

	@Override
	public PushTokenBo findTokenEnableByUserId(String alias) {
		return tokenDao.findTokenEnableByUserId( alias);
	}

}
