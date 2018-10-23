package com.lad.service;

import java.util.List;

import org.springframework.data.geo.GeoResults;

import com.lad.bo.PartyBo;
import com.lad.bo.PartyNoticeBo;
import com.lad.bo.PartyUserBo;
import com.mongodb.CommandResult;
import com.mongodb.WriteResult;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/9/7
 */
public interface IPartyService {

    /**
     * 添加聚会
     * @param partyBo
     * @return
     */
    PartyBo insert(PartyBo partyBo);

    /**
     * 修改聚会
     * @param partyBo
     * @return
     */
    WriteResult update(PartyBo partyBo);

    /**
     * 删除聚会
     * @param id
     * @return
     */
    WriteResult delete(String id);

    /**
     * 查找我发起的聚会
     * @param createid
     * @return
     */
    List<PartyBo> findByCreate(String createid, int page, int limit);

    /**
     * 查找我参与的聚会
     * @return
     */
    List<PartyBo> findByMyJoin(String userid, int page, int limit);

    /**
     * 查找聚会
     * @param id
     * @return
     */
    PartyBo findById(String id);

    /**
     * 查找我申请的聚会
     * @return
     */
    List<PartyBo> findByMyApply(String userid, int page, int limit);


    /**
     * 更新用户
     * @param id
     * @param users
     * @return
     */
    WriteResult updateUser(String id, List<String> users, int userNum);


    /**
     * 查找圈子里所有聚会数量
     * @return
     */
    long getCirclePartyNum(String circleid);


    /**
     * 更新访问
     * @param id
     * @return
     */
    WriteResult updateVisit(String id);

    /**
     * 更新分享
     * @param id
     * @return
     */
    WriteResult updateShare(String id, int num);

    /**
     * 更新分享
     * @param id
     * @return
     */
    WriteResult updateCollect(String id, int num);

    /**
     * 更新举报
     * @param id
     * @return
     */
    WriteResult updateReport(String id, int num);

    /**
     * 报名聚会
     * @param partyUserBo
     * @return
     */
    PartyUserBo addParty(PartyUserBo partyUserBo);


    WriteResult updatePartyUser(PartyUserBo partyUserBo);

    /**
     * 管理报名聚会
     * @param partyid
     * @return
     */
    List<PartyUserBo> findPartyUser(String partyid, int status);

    /**
     * 更新群聊
     * @return
     */
    WriteResult updateChatroom(String partyid, String chatroomid);

    /**
     * 退出聚会
     * @param partyid
     * @return
     */
    WriteResult outParty(String partyid, String userid);

    /**
     * 报名详情查看
     * @param partyid
     * @return
     */
    List<PartyUserBo> findByPartyUsers(String partyid);

    /**
     * 参与的聚会
     * @return
     */
    List<PartyUserBo> findPartyByUserid(String userid, int page, int limit);

    /**
     * 收藏或取消收藏聚会
     * @param partyid
     * @return
     */
    WriteResult collectParty(String partyid, String userid, boolean isCollect);

    /**
     * 查找圈子里所有聚会
     * @return
     */
    List<PartyBo> findByCircleid(String circleid, int page, int limit);

    /**
     * 查找当前用户聚会参与信息
     * @param partyid
     * @return
     */
    PartyUserBo findPartyUser(String partyid, String userid);

    /**
     * 查找当前用户聚会参与信息,不需要判断是否已经删除
     * @param partyid
     * @return
     */
    PartyUserBo findPartyUserIgnoreDel(String partyid, String userid);

    /**
     * 删除参与聚会人员
     * @param partyid
     * @return
     */
    WriteResult deleteMulitByaPartyid(String partyid);

    /**
     * 删除我参与的聚会
     * @param partyid
     * @return
     */
    WriteResult deleteJoinParty(String partyid, String userid);

    /**
     * 退出聚会
     * @param id
     * @return
     */
    WriteResult outParty(String id);

    /**
     * 聚会状态修改
     * @param id
     * @return
     */
    WriteResult updatePartyStatus(String id, int status);

    /**
     * 添加通知
     * @param noticeBo
     * @return
     */
    PartyNoticeBo addPartyNotice(PartyNoticeBo noticeBo);

    /**
     *
     * @param id
     * @return
     */
    PartyNoticeBo findNoticeById(String id);

    /**
     *
     * @param id
     * @return
     */
    WriteResult deleteNotice(String id);

    /**
     *
     * @param partyid
     * @param page
     * @param limit
     * @return
     */
    List<PartyNoticeBo> findNoticeByPartyid(String partyid, int page, int limit);

    /**
     *
     * @param userid
     * @param page
     * @param limit
     * @return
     */
    List<PartyNoticeBo> findNoticeByUserid(String userid, int page, int limit);

    /**
     * 查找通知
     * @param partyid
     * @return
     */
    PartyNoticeBo findPartyNotice(String partyid);

    /**
     * 附近的聚会
     * @param position
     * @param maxDistance
     * @param limit
     * @return
     */
    GeoResults<PartyBo> findNearParty(double[] position, int maxDistance, int limit,int page);

	CommandResult findNearCircleByCommond(double[] position, int i, int limit, int page);

	WriteResult updateShareNum(String partyid);
}
