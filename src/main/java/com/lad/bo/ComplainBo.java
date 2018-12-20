package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@SuppressWarnings("serial")
@Document(collection = "complain")
@Setter
@Getter
@ToString
public class ComplainBo extends BaseBo {

	private String userid;
	private String content;
}
