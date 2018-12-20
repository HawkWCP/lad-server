package com.lad.bo;

import java.util.LinkedHashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述： 聚会管理
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/8/27
 */
@Document(collection = "partyManage")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class PartyManageBo extends BaseBo {

    private String partyid;

    private LinkedHashSet<String> applyUsers = new LinkedHashSet<>();

    private LinkedHashSet<String> users = new LinkedHashSet<>();

    private LinkedHashSet<String> refuseUsers = new LinkedHashSet<>();
    //访问数量
    private int visitNum;
    //分享数量
    private int shareNum;
    //收藏数量
    private int keepNum;
    //举报数量
    private int reportNum;
}
