package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedList;

@Document(collection = "feedback")
public class FeedbackBo extends BaseBo {
	
	private LinkedList<String> images = new LinkedList<>();

	private String content;
	private String contactInfo;

	private String module;

	private String subModule;

	//若是举报， 0 帖子举报， 1 资讯举报
	private int subType;

	private String targetId;

	private String targetTitle;

	private String ownerId;

	//类型 0 反馈， 1举报
	private int type;


	public int getSubType() {
		return subType;
	}

	public void setSubType(int subType) {
		this.subType = subType;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public LinkedList<String> getImages() {
		return images;
	}

	public void setImages(LinkedList<String> images) {
		this.images = images;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getTargetTitle() {
		return targetTitle;
	}

	public void setTargetTitle(String targetTitle) {
		this.targetTitle = targetTitle;
	}

	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getContactInfo() {
		return contactInfo;
	}
	public void setContactInfo(String contactInfo) {
		this.contactInfo = contactInfo;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getSubModule() {
		return subModule;
	}

	public void setSubModule(String subModule) {
		this.subModule = subModule;
	}
}
