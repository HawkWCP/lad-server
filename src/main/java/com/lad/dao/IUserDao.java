package com.lad.dao;

import com.lad.bo.Pager;
import com.lad.bo.UserBo;
import com.mongodb.WriteResult;

import java.util.List;

/**
 * 功能描述：
 * Version: 1.0
 * Time:2017/7/2
 */
public interface IUserDao extends IBaseDao {

    UserBo save(UserBo userBo);

    UserBo updatePassword(UserBo userBo);

    UserBo getUser(String userId);

    List<UserBo> getUserByName(String name);

    UserBo getUserByPhone(String phone);

    UserBo updatePhone(UserBo userBo);

    UserBo updateFriends(UserBo userBo);

    UserBo updateChatrooms(UserBo userBo);

    UserBo updateHeadPictureName(UserBo userBo);

    UserBo updateUserName(UserBo userBo);

    UserBo updateSex(UserBo userBo);

    UserBo updatePersonalizedSignature(UserBo userBo);

    UserBo updateBirthDay(UserBo userBo);

    /**
     * 置顶圈子
     * @param userid
     * @param topCircles
     * @return
     */
    WriteResult updateTopCircles(String userid, List<String> topCircles);

    /**
     * 分页查询数据
     * @param userBo
     * @param pager
     * @return
     */
    Pager selectPage(UserBo userBo, Pager pager);

    WriteResult updateLocation(String phone, String locationid);

    List<UserBo> getAllUser();
}
