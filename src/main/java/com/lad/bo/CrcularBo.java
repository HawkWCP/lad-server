package com.lad.bo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashSet;

@Getter
@Setter
@ToString
@Document(collection = "crcular")
public class CrcularBo  extends  BaseBo{
    private LinkedHashSet<String> targetuids;
    private String title;
    private String content;
    private LinkedHashSet<String> images;
}
