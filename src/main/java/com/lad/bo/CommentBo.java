package com.lad.bo;

import java.util.LinkedHashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：评论 Version: 1.0 Time:2017/6/25
 */
@SuppressWarnings("serial")
@Document(collection = "comment")
@Setter
@ToString
@Getter
public class CommentBo extends BaseBo {

	private String content;

	private String parentid;

	private String userName;
	// 帖子ID
	private String noteid;

	// 评论的 目标ID，根据评论类型而定，note为noteid，不算在里面
	private String targetid;

	// 评论的类型
	private int type;
	// 子分类类型，如资讯健康评论，资讯安全评论等
	private int subType;

	// 贴子的发帖人
	private String ownerid;

	private LinkedHashSet<String> photos;

	private int thumpsubNum;
}
