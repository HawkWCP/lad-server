package com.lad.bo;

import java.util.HashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述： 申请加群聊或加圈子时的理由
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/7/5
 */
@Setter
@Getter
@ToString
@Document(collection = "reason")
public class ReasonBo extends BaseBo {
    //申请加入的圈子
    private String circleid;
    //添加理由
    private String reason;
    //拒绝理由
    private String refues;
    //状态，0 表示申请； 1 表示通过， -1表示b拒绝
    private int status;

    private boolean isNotice;

    private String chatroomid;

    //0 圈子申请， 1 群聊申请
    private int reasonType;

    //是否是圈主或圈子管理员邀请，邀请标识
    private boolean isMasterApply;

    //圈子内未读数据，为了减少数据冗余，放到reason表中
    private int unReadNum;
    
    // 未读帖子列表
    private HashSet<String> unReadSet = new HashSet<>();
    
    /**
     * 是否通过聚会页面加入圈子
     * 0 正常加入圈子，
     * 1 通过聚会页面加入圈子 ,
     * 2 通过二维码扫描进入群聊,当 reasonType为1时有效
     */
    private int addType;
    //id
    private String operUserid;

    private String partyid;
}
