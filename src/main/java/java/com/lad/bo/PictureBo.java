package com.lad.bo;

import java.util.LinkedHashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Document(collection = "picture")
public class PictureBo extends BaseBo {
	private String picName;
	private String ablId;
	private String url;
	private String description;
	// 0 不对外公开;1 部分公开,但屏蔽部分用户;2 部分公开,但仅允许部分用户观看;3 仅好友可观看;4 所有人可看
	private int openLevel;
	private LinkedHashSet<String> allowIds = new LinkedHashSet<>();
	private LinkedHashSet<String> refuseIds = new LinkedHashSet<>();
}
