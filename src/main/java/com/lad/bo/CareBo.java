package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Document(collection="care")
public class CareBo extends BaseBo{
	
	public static final int CARE_CARE = 1;
	public static final int CARE_PASS = 2;
	
	public static final int CARE_RESTHOME = 3; 
	
	// 主体id
	private String uid;
	// 客体id
	private String oid;
	// 客体类型 1.找儿媳;2.找女婿;3.养老院;4找旅友;5.找老伴
	private int objType;
	// 关注类型 1关注,2拉黑
	private int careType;
}
