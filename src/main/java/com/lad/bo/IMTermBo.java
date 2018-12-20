package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "iMTerm")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class IMTermBo extends BaseBo {
	
	private String term;
	private String userid;	
}
