package com.lad.bo;

import java.util.HashSet;
import java.util.LinkedList;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Document(collection = "homepage")
@SuppressWarnings("serial")
public class HomepageBo extends BaseBo {

	private String owner_id;
	private int new_visitors_count;
	private int total_visitors_count;
	private LinkedList<String> visitor_ids = new LinkedList<String>();
	private HashSet<String> not_push_set = new HashSet<>();
	private HashSet<String> hide_record_set = new HashSet<>();
}
