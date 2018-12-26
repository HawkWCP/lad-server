package lad.scrapybo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/8/26
 */
@Getter
@Setter
@Document(collection = "security")
public class SecurityBo extends BaseInforBo{

    private String newsType;

    private String time;

    private String text;

    private String city;


}
