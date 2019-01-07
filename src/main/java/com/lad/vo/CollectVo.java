package com.lad.vo;

import java.util.Date;
import java.util.LinkedHashSet;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/12/14
 */
public class CollectVo extends BaseVo {
	// 收藏本身id
    private String collectid;
    // 收藏内容
    private String content;
    // 收藏资源所属的大框架id,比如收藏帖子,则sourceid的值应该是circleid
	private String sourceid;

    public String getSourceid() {
		return sourceid;
	}

	public void setSourceid(String sourceid) {
		this.sourceid = sourceid;
	}

	// 收藏者id
	private String userid;
	// 标题
    private String title;
    // 路径
    private String path;
    // 大类型: 文件 或 圈子
    private int type;
    //子分类，在url中区分文章、帖子、聚会、圈子
    private int sub_type;
    
    // 收藏目标id
    private String targetid;
    //sourceid对应资源的name
    private String source;
    //收藏来源类型，资讯类型来源分类，1 健康， 2安防， 3 广播， 4 视频， 5 圈子
    private int sourceType;

    private String collectUserid;

    private String collectUserName;

    private String collectUserPic;
    //来源的原始图片，如圈子头像，资讯、帖子第一张图片
    private String collectPic;

    private String video;

    private String videoPic;

    //用户自定义分类
    private LinkedHashSet<String> userTags = new LinkedHashSet<>();

    private Date collectTime;

    //广播和视频合集收藏专用
    private String module;

    private String className;
    //合集第一条信息id
    private String firstid;

    private boolean inforGroups;


    // 帖子是否为转发
    private int noteForward; 
    // 帖子转发的大类型
    private int noteForwardType;
    public int getNoteForwardType() {
		return noteForwardType;
	}

	public void setNoteForwardType(int noteForwardType) {
		this.noteForwardType = noteForwardType;
	}

	// 帖子转发的资讯类型
    private int noteForwardInforType;
    // note下的文件资源类型 : 1. 存文本; 2.图片; 3. 视屏 ; 4. 广播
    private int noteFileType;
    public int getNoteForward() {
		return noteForward;
	}

	public void setNoteForward(int noteForward) {
		this.noteForward = noteForward;
	}

	public int getNoteForwardInforType() {
		return noteForwardInforType;
	}

	public void setNoteForwardInforType(int noteForwardInforType) {
		this.noteForwardInforType = noteForwardInforType;
	}

	public String getNoteForwardSourceId() {
		return noteForwardSourceId;
	}

	public void setNoteForwardSourceId(String noteForwardSourceId) {
		this.noteForwardSourceId = noteForwardSourceId;
	}

	// 帖子转发的资源id
    private String noteForwardSourceId;
    
    public int getNoteFileType() {
		return noteFileType;
	}

	public void setNoteFileType(int noteFileType) {
		this.noteFileType = noteFileType;
	}

	public String getCollectid() {
        return collectid;
    }

    public void setCollectid(String collectid) {
        this.collectid = collectid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSub_type() {
        return sub_type;
    }

    public void setSub_type(int sub_type) {
        this.sub_type = sub_type;
    }

    public String getTargetid() {
        return targetid;
    }

    public void setTargetid(String targetid) {
        this.targetid = targetid;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getSourceType() {
        return sourceType;
    }

    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    public LinkedHashSet<String> getUserTags() {
        return userTags;
    }

    public void setUserTags(LinkedHashSet<String> userTags) {
        this.userTags = userTags;
    }

    public Date getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(Date collectTime) {
        this.collectTime = collectTime;
    }

    public String getCollectUserid() {
        return collectUserid;
    }

    public void setCollectUserid(String collectUserid) {
        this.collectUserid = collectUserid;
    }

    public String getCollectUserName() {
        return collectUserName;
    }

    public void setCollectUserName(String collectUserName) {
        this.collectUserName = collectUserName;
    }

    public String getCollectUserPic() {
        return collectUserPic;
    }

    public void setCollectUserPic(String collectUserPic) {
        this.collectUserPic = collectUserPic;
    }

    public String getCollectPic() {
        return collectPic;
    }

    public void setCollectPic(String collectPic) {
        this.collectPic = collectPic;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getVideoPic() {
        return videoPic;
    }

    public void setVideoPic(String videoPic) {
        this.videoPic = videoPic;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFirstid() {
        return firstid;
    }

    public void setFirstid(String firstid) {
        this.firstid = firstid;
    }

    public boolean isInforGroups() {
        return inforGroups;
    }

    public void setInforGroups(boolean inforGroups) {
        this.inforGroups = inforGroups;
    }
}
