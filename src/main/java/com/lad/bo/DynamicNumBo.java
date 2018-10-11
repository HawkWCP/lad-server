package com.lad.bo;

import java.util.LinkedHashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述： Copyright: Copyright (c) 2017 Version: 1.0 Time:2017/9/23
 */
@Getter
@Setter
@ToString
@Document(collection = "dynamicNum")
public class DynamicNumBo extends BaseBo {

	private String userid;

	// 个人动态数量 帖子数+聚会数+评论+点赞+转发
	private int number;

	// 所有动态
	private long total;
}
