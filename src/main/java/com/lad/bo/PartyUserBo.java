package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述： 参与聚会的人员
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/8/19
 */
@Document(collection = "partyUser")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class PartyUserBo extends BaseBo {

    private String userid;

    private String partyid;

    private String joinPhone;

    private String joinInfo;

    private String refuseInfo;

    private double amount;

    private int userNum;

    // 0 申请， 1已加入 2 拒绝
    private int status;
    //0 未收藏，1 收藏
    private int collectParty;
    //用户是否删除记录信息  0 未删除 ， 1 已删除
    private int userDelete;

}
