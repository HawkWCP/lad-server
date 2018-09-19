package com.lad.bo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 功能描述：用户访问记录
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/12/18
 */
@Setter
@Getter
@ToString
@Document(collection = "userVisit")
public class UserVisitBo implements Serializable {

    @Id
    private String id;
    //被访问人id
    private String ownerid;
    //访问人id
    private String visitid;
    //访问时间
    private Date visitTime = new Date();
    //0 个人主页， 1 动态主页
    private int type;

    private int deleted = 0;
    
    private boolean read = false;
}
