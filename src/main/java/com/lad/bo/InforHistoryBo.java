package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：资讯热度历史记录
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/10/5
 */
@Document(collection = "inforHistory")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class InforHistoryBo extends BaseBo{
    //need index
    private String inforid;
    //need index
    private long dayNum;
    //阅读时间 当天零点的时间戳  need index
    private String readDate;
    //need index
    private int type;
    //need index
    private String module;

    private String className;
}
