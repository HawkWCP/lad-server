package com.lad.bo;

import java.util.HashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Document(collection = "organization")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class OrganizationBo extends BaseBo {
	private double[] position;
	private String landmark;
	private String name;
	private String tag;
	private String sub_tag;
	private String description;
	private HashSet<String> masters = new HashSet<String>();
	private HashSet<String> users = new HashSet<String>();
	private HashSet<String> usersApply = new HashSet<String>();

}
