package com.lad.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PictureVo extends BaseVo {
	private String albId;
	private String albName;
	private String url;
	private String description;
	private String createtime;
	private String createuid;
	private String updatetime;
	private boolean inWall = false;
	private int openLevel;
}
