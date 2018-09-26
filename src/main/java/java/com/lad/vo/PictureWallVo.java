package com.lad.vo;

import java.util.LinkedHashSet;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PictureWallVo extends BaseVo {
	private LinkedHashSet<String> pictures = new LinkedHashSet<>(4);
}
