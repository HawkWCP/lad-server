package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述： Copyright: Copyright (c) 2017 Version: 1.0 Time:2017/9/20
 */
@Document(collection = "dynamicMsg")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class DynamicMsgBo extends BaseBo {

	// 帖子、聚会、动态的id
	private String targetid;

	// 1 帖子、2 聚会、3 动态类型
	private int dynamicType;

	private String userid;
}
