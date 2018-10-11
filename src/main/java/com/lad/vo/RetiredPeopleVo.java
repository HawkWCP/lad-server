package com.lad.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class RetiredPeopleVo extends BaseVo {
	private String wannaArea;
	private String HomeArea;
	private String address;
	private String name;
	private int age;
	private String sex;
	private String serviceLevel;
	private String price;
	private String health;
	private String linkman;
	private String phone;
}
