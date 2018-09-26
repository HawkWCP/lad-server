package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.LinkedList;

@Setter
@Getter
@ToString
@Document(collection = "homepage")
public class HomepageBo extends BaseBo {

	private static final long serialVersionUID = 1L;
	private String owner_id;
	private int new_visitors_count;
	private int total_visitors_count;
	private LinkedList<String> visitor_ids = new LinkedList<String>();
	private HashSet<String> not_push_set = new HashSet<>();
	private HashSet<String> hide_record_set = new HashSet<>();
}
