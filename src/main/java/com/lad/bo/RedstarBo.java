package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：红人计量表
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/6/29
 */
@Document(collection = "redstar")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class RedstarBo extends BaseBo {

    private String circleid;

    private String userid;

    //红人总榜评论数
    private Long commentTotal;

    //红人周榜评论数
    private Long commentWeek;

    //最后更新日期是今年第几周
    private int weekNo;

    //最后更新日期是今年第几周
    private int year;
}
