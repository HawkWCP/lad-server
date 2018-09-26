package com.lad.vo;

import java.util.LinkedHashSet;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AlbumVo  extends BaseVo{
	private String name;
	private String createuid;
	private String albAuthority;
	// 相册级别的返回数据中需要判断当前用户是否有查看权限
	private boolean allow;
	private String albDesc;
	private String createtime;
	private LinkedHashSet<String> top4;
	private int livingNum;
	private int deletedNum;
}
