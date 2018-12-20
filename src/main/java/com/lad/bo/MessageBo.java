package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "message")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class MessageBo extends BaseBo {

	private String userid;
	private String title;
	private String content;
	private String path;
	//阅读状态，0未读， 1已读
	private int status;
	//目标id，帖子
	private String targetid;

	private String circleid;

	// 对贴点赞或者评论表的id
	private String sourceid;
	//0 普通消息，1 评论消息， 2点赞消息
	private int type;
}
