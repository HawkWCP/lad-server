package com.lad.bo;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述： 圈子加入历史，是否加入过圈子 Copyright: Copyright (c) 2017 Version: 1.0
 * Time:2017/9/17
 */
@Getter
@Setter
@ToString
@Document(collection = "circleAdd")
public class CircleAddBo implements Serializable {

	@Id
	private String id;

	private String userid;

	private String circleid;
	// 0.拒绝;1.同意;2.退出
	private int status;

	private Date updateTime;
}
