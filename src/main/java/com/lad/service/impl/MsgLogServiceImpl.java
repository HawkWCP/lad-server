package com.lad.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lad.bo.MsgLogBo;
import com.lad.dao.IMsgLogDao;
import com.lad.service.IMsgLogService;

@Service("msgLogService")
public class MsgLogServiceImpl extends BaseServiceImpl implements IMsgLogService {
	@Autowired
	private IMsgLogDao msgLogDao;

	@Override
	public List<MsgLogBo> findLogByUid(String uuid) {
		return msgLogDao.findLogByUid(uuid);
	}

	@Override
	public List<MsgLogBo> findLogByChannel(String channel,int page,int limit) {
		return msgLogDao.findLogByChannel( channel,page,limit);
	}

	@Override
	public List<MsgLogBo> findLogByChannel(String channel, Long startTs, int page, int limit) {
		return msgLogDao.findLogByChannel( channel,  startTs,  page,  limit);
	}

	@Override
	public boolean checkUserInChannel(String id, String channel) {
		return msgLogDao.checkUserInChannel(id,channel);
	}

}
