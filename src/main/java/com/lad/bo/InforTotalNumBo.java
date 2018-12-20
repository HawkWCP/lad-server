package com.lad.bo;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/12/10
 */
@Document(collection = "inforTotalNumBo")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class InforTotalNumBo implements Serializable{

    @Id
    private String id;

    private String module;

    private String className;

    private int type;

    private int visitTotal;
}
