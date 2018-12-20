package com.lad.bo;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：用户标签 Copyright: Copyright (c) 2017 Version: 1.0 Time:2017/9/28
 */
@Document(collection = "userTag")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class UserTagBo implements Serializable {

	@Id
	private String id;

	private String userid;

	private String tagName;

	// 标签分类 0 用户收藏专用标签
	private int tagType;

	// 标签被使用次数
	private long tagTimes;
}
