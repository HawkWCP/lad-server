package com.lad.bo;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述： 咨询阅读数量表、评论、点赞、转发 Copyright: Copyright (c) 2017 Version: 1.0
 * Time:2017/8/5
 */
@Document(collection = "inforReadNum")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class InforReadNumBo implements Serializable {

	@Id
	private String id;

	private String inforid;

	private String className;

	private long visitNum;

	private int commentNum;

	private int thumpsubNum;

}
