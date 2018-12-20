package com.lad.bo;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：用户阅读资讯最后一条历史表 Copyright: Copyright (c) 2018 Version: 1.0 Time:2018/2/7
 */
@Document(collection = "userReadHis")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class UserReadHisBo implements Serializable {

	@Id
	private String id;

	private String userid;
	// 资讯类型
	private int inforType;

	private String module;

	private String className;
	// 资讯id
	private String inforid;
	// 最后一次阅读时间
	private Date lastTime;
}
