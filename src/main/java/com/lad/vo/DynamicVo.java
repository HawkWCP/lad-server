package com.lad.vo;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述： Copyright: Copyright (c) 2017 Version: 1.0 Time:2017/9/23
 */
@Setter
@Getter
@ToString
public class DynamicVo extends BaseVo {
	
	
	private List<UserBaseVo> atUsers;

	private String title;
	private String content;
	// 动态创建者id
	private String userid;
	private Date time;
	// 动态创建者头像
	private String userPic;
	// 动态创建者用户名
	private String userName;
	// 地理位置
	private String landmark;
	// 经纬度
	private double[] postion;
	// 文件类型
	private String fileType;
	// 照片列表
	private LinkedHashSet<String> photos;
	// 视频缩略图
	private String videoPic;
	// 视频地址
	private String video;
	
	// 当前用户是否点赞
	private Boolean isMyThumbsup;
	private boolean neww = false;

	// 原作者
	private String owner;

	// 来源id
	private int forward;
	private int type;
	private String sourceId;
    //来源名称
    private String sourceName;
	// 转发或分享时点评内容
	private String view;

	
	// 转发量
	private int transNum;
	// 评论数量
	private List<CommentVo> comment;
	// 点赞数量
	private List<ThumbsupBaseVo> thumbsupUser;

	
	// 圈子id
	private String circleid;
	// 圈子名
	private String circleName;
	// 圈子内人数
	private int circleUserNum;
	// 圈子内帖子数
	private int circleNoteNum;
	// 来源类型
	private int sourceType;
	//资讯
	private int inforType;
	private String inforClassName;
	private int showType;
}
