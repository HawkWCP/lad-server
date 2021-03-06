package com.lad.vo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class RequireVo extends BaseVo {
	private String age;			// 年龄要求(范围值)
	private String hight;		// 身高要求(范围值)
	private String education; 	// 学历 0,不限;
	private int marriaged;	// 婚史 0,没有;1,爱过;-1,不限
	private Set<String> job = new HashSet<>();	// 职业要求
	private String salary;		// 收入要求(范围值)
	private String nowin;		// 居住地
	private Map<String,Set<String>> hobbys = new HashMap<>();// 兴趣,list
}
