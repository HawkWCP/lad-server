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

	// 0 帖子点赞； 1 资讯点赞, 5 帖子评论点赞， 6 资讯评论点赞
	private int type;
}
