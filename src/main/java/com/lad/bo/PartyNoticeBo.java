package com.lad.bo;

import java.util.LinkedList;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：聚会通知实体
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/11/24
 */
@Document(collection = "partyNotice")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class PartyNoticeBo extends BaseBo {

    private String title;

    private String content;

    private String partyid;
    //用户
    private LinkedList<String> users = new LinkedList<>();


}
