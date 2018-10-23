package lad.scrapybo;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import lombok.Getter;
import lombok.Setter;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2018
 * Version: 1.0
 * Time:2018/4/5
 */
@Setter
@Getter
public class BaseInforBo implements Serializable{
    @Id
    private String id;
    //阅读
    private int visitNum;
    //分享转发
    private int shareNum;
    //评论
    private int commnetNum;
    //点赞
    private int thumpsubNum;
    //收藏
    private int collectNum;

    private String source;

    private String sourceUrl;

    private String title;

    private String module;

    private String className;

}
