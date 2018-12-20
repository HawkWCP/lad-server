package com.lad.bo;

import java.util.LinkedHashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@SuppressWarnings("serial")
@Getter
@Setter
@ToString
@Document(collection = "resthome")
public class RestHomeBo extends BaseBo {
	// 所在地区
	private String area;
	// 详细地址
	private String address;
	// 姓名
	private String name;
	// 机构类型
	private String type;
	// 性质
	private String property;
	// 管理人
	private String manager;
	// 创建时间
	private String foundTime;
	// 总床位
	private String allSeat;
	// 空闲床位
	private int restSeat;
	// 特殊服务
	private LinkedHashSet<String> serviceLevel = new LinkedHashSet<>();
	// 价格
	private String price;
	// 营业执照地址
	private String licence;
	// 联系人
	private String linkman;
	// 联系人的联系电话
	private String phone;
	// 露陷
	private String ways;
	// 照片集
	private LinkedHashSet<String> images = new LinkedHashSet<>();
	// 接受异地
	private boolean acceptOtherArea;
	// 医疗定点
	private boolean orderPoint;
	// 介绍
	private String introduction;
	
	private boolean protocol = false;
	
	private int shareCount;
	
	private int homeHot;
}
