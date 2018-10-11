package com.lad.bo;

import java.util.LinkedHashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection = "resthome")
public class RestHomeBo extends BaseBo {
	private String area;
	private String address;
	private String name;
	// 机构类型:全部 、养老院、 敬老院 、福利院、 疗养院 、老年公寓 、老人院 、护理院、 养老社区、 养老照料中心 、其它
	private LinkedHashSet<String> type = new LinkedHashSet<>();
	// 机构性质:国营机构 、民营机构 、社会团体 、公办民营、 公助民办 、其它
	private String property;
	private String manager;
	private String foundTime;
	// 床位总量: 50以内 、50-100、 101-200、 201-300、 301-500、 501-1000 、1000以上
	private String allSeat;
	private int restSeat;
	// 收住对象: 全部、 自理、 半自理/介助、 不能自理/介护、 特护
	private LinkedHashSet<String> serviceLevel = new LinkedHashSet<>();
	// 月收费区间: 全部、 500-1000 、1001-2000 、2001-2500 、2501-3000 、3001-5000 、5001-10000、 10000以上
	private LinkedHashSet<String> price = new LinkedHashSet<>();
	// 特色服务： 全部、可接收异地老人、医保定点、无
	private LinkedHashSet<String> knightService = new LinkedHashSet<>();
	private String licence;
	private String linkman;
	private String phone;
	private String ways;
	private LinkedHashSet<String> images = new LinkedHashSet<>();

	private boolean protocol = false;
}
