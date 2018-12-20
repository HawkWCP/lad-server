package com.lad.bo;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：圈子最新内容展示内容 Copyright: Copyright (c) 2018 Version: 1.0 Time:2018/1/9
 */
@Document(collection = "circleShow")
@SuppressWarnings("serial")
@Setter
@Getter
@ToString
public class CircleShowBo implements Serializable {

	@Id
	private String id;
	// 0 帖子， 1聚会 ，2 资讯
	private int type;
	// 资讯类型
	private int inforType;

	private String targetid;
	// 圈子id
	private String circleid;
	// 创建时间
	private Date createTime;
	// 创建者id
	private String createuid;

}
