package com.lad.dao;

import java.util.HashSet;
import java.util.List;

import com.lad.bo.ReasonBo;
import com.mongodb.WriteResult;

/**
 * 功能描述：
 * Version: 1.0
 * Time:2017/7/5
 */
public interface IReasonDao extends IBaseDao{


    ReasonBo insert(ReasonBo reasonBo);

    /**
     * 是否同意
     * @param id
     * @return
     */
    WriteResult updateApply(String id, int status, String refuse);

    /**
     * 查找申请添加信息
     * @param userid   申请人id
     * @return
     */
    ReasonBo findByUserAndCircle(String userid, String circleid, int status);

    /**
     * 查找申请添加信息
     * @return
     */
    ReasonBo findById(String id);

    /**
     * 删除申请信息
     * @return
     */
    WriteResult deleteById(String id);

    /**
     * 查找圈子所有的申请添加信息
     * @return
     */
    List<ReasonBo> findByCircle(String circleid);

    /**
     * 查找圈子申请历史记录
     * @return
     */
    List<ReasonBo> findByCircleHis(String circleid, int page, int limit);

    /**
     * 查找所有聊天室申请
     * @param chatroomid
     * @return
     */
    List<ReasonBo> findByChatroomHis(String chatroomid, int page, int limit);

    /**
     * 查找所有聊天室申请
     * @param chatroomid
     * @return
     */
    List<ReasonBo> findByChatroom(String chatroomid);

    /**
     * 查找单个申请
     * @param userid
     * @param chatroomid
     * @return
     */
    ReasonBo findByUserAndChatroom(String userid, String chatroomid);

    /**
     * 查找所有聊天室申请
     * @param chatroomid
     * @return
     */
    List<ReasonBo> findByChatroom(String chatroomid, int status);

    /**
     * 查找单个申请
     * @param userid
     * @param chatroomid
     * @return
     */
    ReasonBo findByUserAndChatroom(String userid, String chatroomid, int status);


    /**
     * 是否管理员邀请修改
     * @param id
     * @return
     */
    WriteResult updateMasterApply(String id, int status, boolean isMasterApply);


    /**
     * 更新未读数量信息
     * @param userid
     * @param circleid
     * @param num
     * @return
     */
    WriteResult updateUnReadNum(String userid, String circleid, int num);

    /**
     * 清零未读数量
     * @param id
     * @return
     */
    WriteResult updateUnReadNumZero(String id);

    /**
     * 清零未读数量信息
     * @param userid
     * @param circleid
     * @return
     */
    WriteResult updateUnReadNumZero(String userid, String circleid);

    /**
     * 更新未读数量信息
     * @param userids
     * @param circleid
     * @return
     */
    WriteResult updateUnReadNum(HashSet<String> userids, String circleid);

    /**
     * 查找申请添加信息
     * @param userid   申请人id
     * @return
     */
    ReasonBo findByUserAdd(String userid, String circleid);


    /**
     * 批量删除用户
     * @param removeUsers
     * @param circleid
     * @return
     */
    WriteResult removeUser(HashSet<String> removeUsers, String circleid);

    /**
     * 删除用户
     * @param userid
     * @param circleid
     * @return
     */
    WriteResult removeUser(String userid, String circleid);

	List<ReasonBo> findByUserAndCircle(HashSet<String> users, String circleid, int status);

	WriteResult updateUnReadSet(String userid, String circleid, HashSet<String> unReadSet);

	ReasonBo findByUserAndCircle(String userid, String circleid, int status, int reasonType);

	List<ReasonBo> findByUserAddChatroom(String userid);
}
