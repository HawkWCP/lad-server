package com.lad.bo;

import java.util.LinkedList;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "feedback")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class FeedbackBo extends BaseBo {
	
	private LinkedList<String> images = new LinkedList<>();
	//举报内容
	private String content;
	//举报联系
	private String contactInfo;
	//举报分类
	private String module;
	//举报子分类
	private String subModule;

	//若是举报， 0 帖子举报， 1 资讯举报, 2 圈子举报
	private int subType;
	//举报目标ID
	private String targetId;
	//举报目标标题
	private String targetTitle;
	//举报人id
	private String ownerId;

	//类型 0 反馈， 1举报
	private int type;
}
