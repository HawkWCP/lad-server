package com.lad.bo;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/8/18
 */
@Document(collection = "party")
@ToString
@Setter
@Getter
@SuppressWarnings("serial")
public class PartyBo extends BaseBo {

    private String title;

    private String content;

    private String circleid;

    //聚会背景图片
    private String backPic;

    private LinkedList<String> photos;

    private String video;
    //视频缩略图
    private String videoPic;
    //聚会时间
    private LinkedHashSet<String> startTime;
    //地点类型 0 线上； 1线下
    private int addrType;
    //具体地点，线上必填
    private String addrInfo;
    //线下地点位置
    private double[] position;
    //线下地标
    private String landmark;
    //0 free， 1 收费
    private int payOrFree;
    //收费金额
    private double payAmount;
    //收费名称
    private String payName;
    //收费详情对象
    private String payInfo;
    //预约 天数
    private int appointment;
    //聚会人员数量
    private int userLimit;
    //是否需要填写手机号码
    private boolean isPhone;
    //是否公开
    private boolean isOpen;
    //参加用户温馨提示
    private String reminder;
    //聚会状态 1 进行中， 2 报名结束 ，3 活动结束
    private int status;
    //发起群聊聚会ID
    private String chatroomid;
    //申请用户
    private LinkedList<String> users = new LinkedList<>();
    //访问数量
    private int visitNum;
    //分享数量
    private int shareNum;
    //收藏数量
    private int collectNum;
    //举报数量
    private int reportNum;
    //聚会人数,包括报名的和报名时额外添加的人数
    private int partyUserNum;

    //0 原创 ， 1转发,特指转发到圈子
    private int forward;
    //转发时评论信息
    private String view;
    //来源聚会id
    private String sourcePartyid;
}
