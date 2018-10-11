package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection = "retiredpeople")
public class RetiredPeopleBo extends BaseBo {
	private String wannaArea;
	private String homeArea;
	private String address;
	private String name;
	private int age;
	private String sex;
	private String serviceLevel;
	private String price;
	private String health;
	private String linkman;
	private String phone;
	
	private boolean protocol = false;
}
