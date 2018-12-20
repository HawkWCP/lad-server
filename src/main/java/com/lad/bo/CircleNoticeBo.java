package com.lad.bo;

import java.util.LinkedHashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：圈子公告
 * Copyright: Copyright (c) 2018
 * Version: 1.0
 * Time:2018/1/4
 */
@Document(collection = "circleNotice")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")

public class CircleNoticeBo extends BaseBo {
	//标题
    private String title;
    //内容
    private String content;
    //圈子id
    private String circleid;
    //操作类型:0 添加， 1修改，2删除
    private int type;
    //0 圈子公告， 1 群公告
    private int noticeType;
    //群聊id ，因需求变更，群聊公告和圈子公告一直，添加群聊公告类型
    private String chatroomid;
    //图片
    private LinkedHashSet<String> images = new LinkedHashSet<>();
    //未阅读者
    private LinkedHashSet<String> unReadUsers;
    //已阅读者
    private LinkedHashSet<String> readUsers = new LinkedHashSet<>();
}
