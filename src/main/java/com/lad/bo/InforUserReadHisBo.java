package com.lad.bo;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：分类用户最后阅读时间
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/10/24
 */
@Document(collection = "inforUserReadHis")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class InforUserReadHisBo implements Serializable {

    @Id
    private String id;
    //一级分类
    private String module;
    //二级分类
    private String className;

    private int type;

    private String lastDate;

    private String userid;
}
