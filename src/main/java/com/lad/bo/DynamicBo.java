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

    private String picType;
    //视频缩略图
    private String videoPic;

    private String video;

    private int type;

    //原作者
    private String owner;
    //来源名称
    private String sourceName;
    //来源
    private String sourceid;
    
	private LinkedHashSet<String> unReadFrend = new LinkedHashSet<>();
	
}
