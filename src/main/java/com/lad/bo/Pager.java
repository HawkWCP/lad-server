package com.lad.bo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Pager {

	private int pageSize = 10;

	private int pageNum = 1;

	private int pageCount;

	private long total;

	@SuppressWarnings("rawtypes")
	private List result;
}
