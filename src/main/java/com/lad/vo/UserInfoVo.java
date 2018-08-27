package com.lad.vo;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/12/18
 */
@Setter
@Getter
@ToString
public class UserInfoVo extends UserBaseVo {

    private LinkedHashSet<String> sports;

    private LinkedHashSet<String> musics;

    private LinkedHashSet<String> lifes;

    private LinkedHashSet<String> trips;
    
    private Date registTime;

    private double postion[];
}
