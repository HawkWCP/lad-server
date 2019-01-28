package com.lad.vo;

import java.util.LinkedHashSet;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CrcularVo {
	private String title;
	private String content;
	private LinkedHashSet<String> images;
	// 0 未读;1 已读
	private boolean isread;
	
	private String path;
}
