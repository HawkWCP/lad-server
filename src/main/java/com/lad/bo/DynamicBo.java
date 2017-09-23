package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashSet;

/**
 * 功能描述：个人动态
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/9/19
 */
@Document(collection = "dynamic")
public class DynamicBo extends BaseBo {

    //标题
    private String title;
    //内容
    private String content;
    //图片
    private LinkedHashSet<String> images = new LinkedHashSet<>();
    //经纬度
    private double[] postion;
    //转发量
    private int transNum;
    //评论数量
    private int commentNum;
    //点赞数量
    private int thumpNum;
    //地理位置
    private String landmark;

    private String picType;
    //视频缩略图
    private String videoPic;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LinkedHashSet<String> getImages() {
        return images;
    }

    public void setImages(LinkedHashSet<String> images) {
        this.images = images;
    }

    public double[] getPostion() {
        return postion;
    }

    public void setPostion(double[] postion) {
        this.postion = postion;
    }

    public int getTransNum() {
        return transNum;
    }

    public void setTransNum(int transNum) {
        this.transNum = transNum;
    }

    public int getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(int commentNum) {
        this.commentNum = commentNum;
    }

    public int getThumpNum() {
        return thumpNum;
    }

    public void setThumpNum(int thumpNum) {
        this.thumpNum = thumpNum;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public String getPicType() {
        return picType;
    }

    public void setPicType(String picType) {
        this.picType = picType;
    }

    public String getVideoPic() {
        return videoPic;
    }

    public void setVideoPic(String videoPic) {
        this.videoPic = videoPic;
    }
}