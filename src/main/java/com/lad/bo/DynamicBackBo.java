package com.lad.bo;

import java.util.HashSet;
import java.util.LinkedHashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：个人动态 黑名单设置 Copyright: Copyright (c) 2017 Version: 1.0 Time:2017/9/21
 */
@SuppressWarnings("serial")
@Document(collection = "dynamicBack")
@Setter
@Getter
@ToString
public class DynamicBackBo extends BaseBo {

	private String userid;

	// 我不看谁 黑名单
	private HashSet<String> notSeeBacks = new LinkedHashSet<>();

	// 不让谁看我 黑名单
	private HashSet<String> notAllowBacks = new LinkedHashSet<>();
}
