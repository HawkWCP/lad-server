package com.lad.dao;

import java.util.List;

import com.lad.bo.MsgLogBo;

public interface IMsgLogDao {

	List<MsgLogBo> findLogByUid(String uuid);

	List<MsgLogBo> findLogByChannel(String channel,int page,int limit);

	List<MsgLogBo> findLogByChannel(String channel, Long startTs, int page, int limit);

	boolean checkUserInChannel(String id, String channel);

}
