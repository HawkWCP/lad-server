package com.lad.bo;

import java.util.LinkedHashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：个人动态
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/9/19
 */
@Getter
@Setter
@ToString
@Document(collection = "dynamic")
public class DynamicBo extends BaseBo {
    //信息来源id
    private String msgid;
    //标题
    private String title;
    //转发或分享时点评内容
    private String view;
    //内容
    private String content;
    //图片
    private LinkedHashSet<String> photos = new LinkedHashSet<>();
    //经纬度
    private double[] postion;
    //转发量
    private int transNum;
    //评论数量
    private int commentNum;
    //点赞数量
    private int thumpNum;
    //地理位置
    private String landmark;
    //照片类型
    private String picType;
    //视频缩略图
    private String videoPic;
    //视屏地址
    private String video;
    //转发类型
    private int type;

    //原作者
    private String owner;
    //来源名称
    private String sourceName;
    //来源
    private String sourceid;

	private LinkedHashSet<String> unReadFrend = new LinkedHashSet<>();
	
	//1健康，2 安防，3广播，4视频，5时政，6养老
	private int inforType;
	
	
	public static final int ALLOW_ALL =0;
	public static final int ALLOW_FRIEND =1;
	public static final int ALLOW_PART =2;
	public static final int ALLOW_NONE =3;
	// 0 所有人可访问,1 好友可访问,2 指定人可访问,3 所有人不可访问;
	private int access_level=0;
	
	// 如果access_level = 1或2,设置一下集合
	// 当等于1 时,access_allow_set = 好友集合
	private LinkedHashSet<String> access_allow_set;
	
	// 视频,广播
	private String inforClassName;
}
