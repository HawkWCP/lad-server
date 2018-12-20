package com.lad.bo;

import java.util.LinkedHashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "tag")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class TagBo extends BaseBo {
	
	private String userid;
	
	private LinkedHashSet<String> friendsIds = new LinkedHashSet<String>();
	//标签
	private String name;
}
