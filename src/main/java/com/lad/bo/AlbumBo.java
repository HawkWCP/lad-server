package com.lad.bo;

import java.util.LinkedHashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document(collection="album")
@Getter
@Setter
@ToString
public class AlbumBo extends BaseBo {
	private String name;
	private String albDesc;
	// 0 不对外公开;1 部分公开,但屏蔽部分用户;2 部分公开,但仅允许部分用户观看;3 仅好友可观看;4 所有人可看
	private int openLevel = 4;
	private LinkedHashSet<String> allowIds = new LinkedHashSet<>();
	private LinkedHashSet<String> refuseIds = new LinkedHashSet<>();
}
