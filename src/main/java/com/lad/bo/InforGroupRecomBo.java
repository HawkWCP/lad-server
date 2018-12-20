package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：二级分类资讯总体的热度，以及180天内的热度， 视频和广播
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/12/10
 */
@Document(collection = "inforGroupRecom")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class InforGroupRecomBo extends BaseBo {

    private String module;

    private String className;

    private int type;

    private long totalNum;

    private long halfyearNum;
}
