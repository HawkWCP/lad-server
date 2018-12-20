package com.lad.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2018
 * Version: 1.0
 * Time:2018/1/16
 */
@Getter
@Setter
public class FriendDisVo extends FriendsVo {

    private boolean star;

    private Date starTime;

    private int sort = Integer.MAX_VALUE;

    private double distance;
    
    private int level;
    
    private int age;
}
