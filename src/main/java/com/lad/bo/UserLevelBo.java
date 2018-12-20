package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述： 用户等级 Copyright: Copyright (c) 2017 Version: 1.0 Time:2017/8/23
 */
@Document(collection = "userLevel")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class UserLevelBo extends BaseBo {

	private String userid;
	// 在线时间 小时
	private double onlineHours;
	// 发起聚会数
	private int launchPartys;
	// 发帖数
	private int noteNum;
	// 评论数
	private int commentNum;
	// 转发数
	private int transmitNum;
	// 分享数
	private int shareNum;
	// 圈子数量
	private int circleNum;
}
