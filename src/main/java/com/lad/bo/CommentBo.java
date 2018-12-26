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
	private String visitor_id;
	private String userName;
	// 贴子的发帖人
	private String ownerid;
	// 评论的 目标ID，根据评论类型而定，note为noteid，不算在里面
	private String targetid;
	// 评论对象类型:	在添加一条评论时,我们需要做的是将对象作为参数传入到评论添加的方法中,该方法将解析评论对象的类型
	// 1 帖子;2:资讯健康;3:评论;4;动态;5;曝光台;6:聚会;7:资讯安防;8:资讯广播;9:资讯视频;10:资讯时政;11:资讯养老
	private int targetType;
	private String content;


	private LinkedHashSet<String> photos;

	private int thumpsubNum;
	
	
	private String noteid;
	private String parentid;
	// 评论的类型
	private int type;
	// 子分类类型，如资讯健康评论，资讯安全评论等
	private int subType;
}
