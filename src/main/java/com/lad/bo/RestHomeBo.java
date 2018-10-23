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
	private LinkedHashSet<String> images = new LinkedHashSet<>();
	private boolean acceptOtherArea;
	private boolean orderPoint;
	
	private boolean protocol = false;
}
