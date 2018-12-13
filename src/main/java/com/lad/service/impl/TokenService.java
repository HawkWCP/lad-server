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
	public PushTokenBo findHuaweiTokenByUserId(String userId) {
		return tokenDao.findHuaweiTokenByUserId(userId);
	}

	@Override
	public PushTokenBo findXiaomiRegIdByUserId(String userId) {
		return tokenDao.findXiaomiRegIdByUserId(userId);
	}
	
	@Override
	public WriteResult updateToken(PushTokenBo tokenBo) {
		return tokenDao.updateToken(tokenBo);
	}

	@Override
	public PushTokenBo insert(PushTokenBo tokenBo) {
		return tokenDao.insert(tokenBo);
	}

	@Override
	public List<PushTokenBo> findTokenByUserIds(Collection<String> useridSet,int type) {
		return tokenDao.findTokenByUserIds(useridSet,type);
	}

	@Override
	public void deletedTokenByTokenAndUserId(String token, String userId,int type) {
		tokenDao.deletedTokenByTokenAndUserId(token, userId,type);
	}

	@Override
	public WriteResult updateOtherStatus(String token, String userId,int type) {
		return tokenDao.updateOtherStatus( token,  userId,type);
	}

	@Override
	public PushTokenBo findTokenByUserIdAndToken(String userId, String token,int type) {
		return tokenDao.findTokenByUserIdAndToken( userId,  token,type);
	}

	@Override
	public WriteResult closeTokenByUseridAndToken(String userId, String token,int type) {
		return tokenDao.closeTokenByUseridAndToken( userId,  token,type);
	}

	@Override
	public PushTokenBo findTokenEnableByUserId(String alias,int type) {
		return tokenDao.findTokenEnableByUserId( alias,type);
	}




}
