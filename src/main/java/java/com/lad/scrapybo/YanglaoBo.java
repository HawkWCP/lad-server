package com.lad.scrapybo;

import java.util.LinkedList;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2018
 * Version: 1.0
 * Time:2018/4/5
 */
@Getter
@Setter
@Document(collection = "yanglao")
public class YanglaoBo extends BaseInforBo {

    private LinkedList<String> imageUrls;

    private String time;

    private String text;

    private int num;

}
