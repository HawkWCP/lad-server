package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：资讯总体的热度，以及180天内的热度 Copyright: Copyright (c) 2017 Version: 1.0
 * Time:2017/10/9
 */
@Document(collection = "inforRecom")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class InforRecomBo extends BaseBo {

	private String inforid;
	// 资讯类型
	private int type;

	private long totalNum;

	private long halfyearNum;

	private String module;
}
