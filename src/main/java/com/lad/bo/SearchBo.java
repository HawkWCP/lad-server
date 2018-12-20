package com.lad.bo;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述： 搜索关键词 Copyright: Copyright (c) 2017 Version: 1.0 Time:2017/9/4
 */
@Document(collection = "search")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class SearchBo implements Serializable {

	@Id
	private String id;

	private String keyword;

	private long times;

	// 0 未删除 ； 1 删除
	private Integer deleted = 0;

	// 0 圈子， 1帖子， 2 资讯, 4 城市
	private Integer type;
	// 资讯搜索的分类，与资讯分类对应
	private int inforType;
}
