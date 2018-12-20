package com.lad.bo;

import java.util.Date;
import java.util.LinkedHashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：发布信息类实体
 * Copyright: Copyright (c) 2018
 * Version: 1.0
 * Time:2018/4/9
 */

@Getter
@Setter
@Document(collection = "release")
@ToString
@SuppressWarnings("serial")
public class ReleaseBo extends BaseBo {

    /**
     * 找老伴信息
     */
    public static final int OLD_CP = 1;

    /**
     * 找儿媳 女婿信息
     */
    public static final int CHILD_CP = 2;

    /**
     * 找驴友信息
     */
    public static final int TOUR_CP = 3;

    //发布类型
    private int releaseType;
    //发布信息内容简介 type=all
    private String brief;

    // 省 type=1/2
    private String province;
    // 市 type=1/2
    private String city;
    // 起始年龄 type=1/2
    private int startAge;
    // 结束年龄 type=1/2
    private int endAge;
    // 个人图片工资 type=1/2
    private LinkedHashSet<String> images;
    // 性别要求 type=1/2
    private String needSex;

    // 起始工资 type=2
    private double startWages;
    // 结束工资 type=2
    private double endWages;
    // 子女简介 type=2
    private String childBrief;
    // 住房要求 type=2
    private String houseNeed;


    // 起始日期 type=3
    private Date startDate;
    // 结束工资 type=3
    private Date endDate;
    // 旅行目的地 type=3
    private LinkedHashSet<String> destinations;
    // 是否自驾 type=3
    private boolean selfDrive;
}
