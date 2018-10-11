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
	private LinkedHashSet<String> type = new LinkedHashSet<>();
	private String property;
	private String manager;
	private String foundTime;
	private String allSeat;
	private int restSeat;
	private LinkedHashSet<String> serviceLevel = new LinkedHashSet<>();
	private LinkedHashSet<String> price = new LinkedHashSet<>();
	private LinkedHashSet<String> knightService = new LinkedHashSet<>();
	private String licence;
	private String linkman;
	private String phone;
	private String ways;
	private LinkedHashSet<String> images = new LinkedHashSet<>();
}
