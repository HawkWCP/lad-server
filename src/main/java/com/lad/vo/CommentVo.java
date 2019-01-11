package com.lad.vo;

import java.util.Date;
import java.util.LinkedHashSet;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/8/5
 */
@Setter
@Getter
@ToString
public class CommentVo extends BaseVo {

	private String commentId;
	
    private String targetId;

    private String content;

    private String parentid;
    //回复的用户名称
    private String parentUserName;

    private String parentUserid;

    private String userName;

    private String userid;

    private Date createTime;

    private long thumpsubCount;

    private Boolean isMyThumbsup;

    private String userHeadPic;

    private String userSex;

    private String userBirth;

    private int userLevel;

    private LinkedHashSet<String> photos;
}
