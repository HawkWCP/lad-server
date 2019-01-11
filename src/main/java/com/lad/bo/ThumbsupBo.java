package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "thumbsup")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class ThumbsupBo extends BaseBo {

	public static final int THUMBSUP_NOTE=1;
	public static final int THUMBSUP_INFOR=2;
	public static final int THUMBSUP_COMMENT=3;
	public static final int THUMBSUP_DYNAMIC=4;
	public static final int THUMBSUP_PARTY=5;
	public static final int THUMBSUP_EXPOSE=6;
	public static final int THUMBSUP_HOMEPAGE=7;
	
	private String homepage_id;
	// 被访问对象id
	private String owner_id;
	// 访问者id
	private String visitor_id;

	// 点赞人的头像图片
	private String image;
	// 1 帖子,2 资讯 3.评论 4. 动态 5. 聚会 6. 曝光台 7. 个人主页
	private int type;
}
