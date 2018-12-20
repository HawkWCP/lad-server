package com.lad.bo;

import java.io.Serializable;
import java.util.LinkedHashSet;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述： 用于阅读分类信息
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/10/12
 */
@Document(collection = "inforUserRead")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class InforUserReadBo implements Serializable {
    @Id
    private String id;

    private String userid;
    //已经阅读过的健康
    private LinkedHashSet<String> healths = new LinkedHashSet<>();
    //安全
    private LinkedHashSet<String> securitys = new LinkedHashSet<>();
    //广播
    private LinkedHashSet<String> radios = new LinkedHashSet<>();
    //视频
    private LinkedHashSet<String> videos = new LinkedHashSet<>();
    
    private LinkedHashSet<String> yanglao = new LinkedHashSet<>();
    
    private LinkedHashSet<String> daily = new LinkedHashSet<>();
	private Integer deleted = 0;
}
