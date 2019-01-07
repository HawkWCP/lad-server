package com.lad.vo;

import java.io.Serializable;
import java.util.LinkedList;

import lombok.Getter;
import lombok.Setter;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/8/9
 */
@Getter
@Setter
public class UserBaseVo implements Serializable {

    private boolean star;
    private String id;

    private String userName;

    private String backName;
    
    private String phone;

    private String sex;

    private String headPictureName;

    private String birthDay;

    private String personalizedSignature;

    private int level;

    private int role;
    
    private String address;

    private LinkedList<String> picTop4;
}
