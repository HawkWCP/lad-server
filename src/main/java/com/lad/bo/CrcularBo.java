package com.lad.bo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.LinkedHashSet;

@Getter
@Setter
@ToString
@Document(collection = "crcular")
public class CrcularBo  extends  BaseBo{
    private String targetuids;
    private String title;
    private String content;
    private LinkedHashSet<String> images;
    // 0 未读;1 已读
    private Integer status;
    // 1. 聚会通知,2.加圈申请,
    private String path;
}
