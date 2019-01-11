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
	// 评论者id
	private String visitor_id;
	// 评论者name
	private String userName;
	private String headPicture;
	// 被评论内容的创建者id
	private String ownerid;
	// 评论的 目标ID
	private String targetid;
	// 评论对象类型:	在添加一条评论时,我们需要做的是将对象作为参数传入到评论添加的方法中,该方法将解析评论对象的类型
	// 1 帖子;2:资讯健康;3:评论;4;动态;5;曝光台;6:聚会;7:资讯安防;8:资讯广播;9:资讯视频;10:资讯时政;11:资讯养老
	private int targetType;
	private String content;
	// 被评论的主体的id,如评论一条帖子,sourceId为帖子id,评论一条帖子下的评论,sourceId仍然为该帖子id,他的作用是在查询数据库是可以一次性获取该帖子的所有评论
	// 与targetid的区别是:targetId指向被评论的直接对象,而这个直接对象可能不是被评论主体的id,而是另一条评论
	private String sourceId;
	private LinkedHashSet<String> photos;
	
	/*该数据应当移植缓存*/
	private int thumpsubNum;
	
	/*一下字段会在后期更新中清理*/
	private String noteid;
	private String parentid;
	// 评论的类型
	private int type;
	// 子分类类型，如资讯健康评论，资讯安全评论等
	private int subType;
}
