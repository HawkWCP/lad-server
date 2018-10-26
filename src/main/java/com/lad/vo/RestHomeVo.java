package com.lad.vo;

import java.util.LinkedHashSet;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RestHomeVo extends BaseVo {	
	private String area;
	private String address;
	private String name;
	private String type;
	private String property;
	private String manager;
	private String foundTime;
	private String allSeat;
	private int restSeat;
	private LinkedHashSet<String> serviceLevel = new LinkedHashSet<>();
	private String price;
	private String licence;
	private String linkman;
	private String phone;
	private String ways;
	private boolean acceptOtherArea;
	private boolean orderPoint;
	
	private String introduction;
	
	private LinkedHashSet<String> images = new LinkedHashSet<>();
}
