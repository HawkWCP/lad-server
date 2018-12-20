package com.lad.bo;

import java.util.LinkedHashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 收藏的聊天信息
 */
@SuppressWarnings("serial")
@Document(collection = "collect")
@Setter
@Getter
@ToString
public class CollectBo extends BaseBo {

	private String content;

	private String userid;

	private String title;

	private String path;

	private int type;
	// 子分类，在url中区分文章、帖子、聚会、圈子
	private int sub_type;

	private String targetid;
	// 来源
	private String source;
	// 来源类型 0 圈子， 1 资讯
	private int sourceType;
	// 来源id，
	private String sourceid;
	// 来源id，
	private String targetPic;

	private String video;

	// 广播和视频合集收藏专用
	private String module;

	private String className;
	// 合集第一条信息id
	private String firstid;

	// 用户自定义分类
	private LinkedHashSet<String> userTags = new LinkedHashSet<>();
}
