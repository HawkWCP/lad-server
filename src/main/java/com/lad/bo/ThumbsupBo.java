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

	private String homepage_id;
	private String owner_id;
	private String visitor_id;

	// 点赞人的头像图片
	private String image;
	// 1 帖子,2 资讯 3.评论 4. 动态 5. 聚会 6. 曝光台 7. 个人主页
	private int type;
}
