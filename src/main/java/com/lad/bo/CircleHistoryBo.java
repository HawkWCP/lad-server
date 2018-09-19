package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：个人的圈子操作记录 Version: 1.0 Time:2017/8/16
 */
@Document(collection = "circleHistory")
@ToString
@Setter
@Getter
public class CircleHistoryBo extends BaseBo {
	// 访问用户id
	private String userid;
	// 圈子id
	private String circleid;
	// 圈子地址
	private double[] position;
	// 访问记录类型， 0圈子访问， 1 圈子操作 ,2 圈子置顶
	private int type;
	// 操作标题
	private String title;
	// 操作内容
	private String content;
	// 操作用户id
	private String operateid;
}
