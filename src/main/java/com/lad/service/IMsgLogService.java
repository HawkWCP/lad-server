package com.lad.service;

import java.util.List;

import com.lad.bo.MsgLogBo;

public interface IMsgLogService {

	List<MsgLogBo> findLogByUid(String uuid);

	List<MsgLogBo> findLogByChannel(String channel,int page,int limit);

	List<MsgLogBo> findLogByChannel(String channel, Long startTs, int page, int limit);

	boolean checkUserInChannel(String id, String channel);

}
