package com.lad.bo;

import java.util.LinkedHashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import com.lad.constants.GeneralContants;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：个人动态 Copyright: Copyright (c) 2017 Version: 1.0 Time:2017/9/19
 */
@SuppressWarnings("serial")
@Getter
@Setter
@ToString
@Document(collection = "dynamic")
public class DynamicBo extends BaseBo {
	/* ==============================基础字段================================= */

	// 标题
	private String title;
	// 内容
	private String content;

	/* ==============================资源相关字段================================= */

	// 文件类型
	private String fileType;
	// 图片
	private LinkedHashSet<String> photos = new LinkedHashSet<>();
	// 视频缩略图
	private String videoPic;
	// 视屏地址
	private String video;
	// 经纬度
	private double[] postion;
	// 地理位置
	private String landmark;
	
	private LinkedHashSet<String> atIds;

	/* ==============================转发相关字段================================= */

	// 是否转发:0 非转发,1,转发对应UserCenterConstants中的FORWARD_TURE和FORWARD_FALSE两个值
	private int forward = GeneralContants.NO;
	// 转发或分享时点评内容
	private String view;
	// 转发类型,转发自资讯:1,转发自圈子:3,转发自帖子:0,转发自聚会:4,转发自养老院:31
	private int type;

	// 来源名称 资讯则标注二级分类名,帖子则标注圈子名,聚会则标注圈子名
	private String sourceName;
	// 原作者
	private String owner;
	// 来源
	private String sourceId;

	/* ==============================动态数据================================= */
	// 转发量
	private int transNum = 0;
	// 评论数量
	private int commentNum = 0;
	// 点赞数量
	private int thumpNum = 0;
	// 未读者集合--默认情况加在创建时会将所有好友放入该集合
	private LinkedHashSet<String> unReadFrend = new LinkedHashSet<>();

	/* ===============================访问权限控制=============================== */

	// 0 所有人可访问,1 好友可访问,2 指定人可访问,3 所有人不可访问;
	private int access_level = 0;
	// 如果access_level = 1或2,设置该参数,当等于1 时,access_allow_set = 好友集合
	private LinkedHashSet<String> access_allow_set;

	/*
	 * =================================个性化=======================================
	 */
	// 1健康，2 安防，3广播，4视频，5时政，6养老
	private int inforType;
	// 视频,广播
	private String inforClassName;
	private int showType;

	// 信息来源id
//  private String msgid;

	// 照片类型
//	private String picType;
}
