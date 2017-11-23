package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/8/18
 */
@Document(collection = "party")
public class PartyBo extends BaseBo {

    private String title;

    private String content;

    private String circleid;

    //聚会背景图片
    private String backPic;

    private LinkedHashSet<String> photos;

    private String video;
    //视频缩略图
    private String videoPic;
    //聚会时间
    private LinkedHashSet<String> startTime;
    //地点类型 0 线上； 1线下
    private int addrType;
    //具体地点，线上必填
    private String addrInfo;
    //线下地点位置
    private double[] position;
    //线下地标
    private String landmark;
    //0 free， 1 收费
    private int payOrFree;
    //收费金额
    private double payAmount;
    //收费名称
    private String payName;
    //收费详情对象
    private String payInfo;
    //预约 天数
    private int appointment;
    //聚会人员数量
    private int userLimit;
    //是否需要填写手机号码
    private boolean isPhone;
    //是否公开
    private boolean isOpen;
    //参加用户温馨提示
    private String reminder;
    //聚会状态 0 进行中， 1 报名结束 ，2 活动结束  3 已取消
    private int status;
    //发起群聊聚会ID
    private String chatroomid;
    //申请用户
    private LinkedList<String> users = new LinkedList<>();
    //访问数量
    private int visitNum;
    //分享数量
    private int shareNum;
    //收藏数量
    private int collectNum;
    //举报数量
    private int reportNum;
    //聚会人数,包括报名的和报名时额外添加的人数
    private int partyUserNum;

    public LinkedList<String> getUsers() {
        return users;
    }

    public void setUsers(LinkedList<String> users) {
        this.users = users;
    }

    public int getVisitNum() {
        return visitNum;
    }

    public void setVisitNum(int visitNum) {
        this.visitNum = visitNum;
    }

    public int getShareNum() {
        return shareNum;
    }

    public void setShareNum(int shareNum) {
        this.shareNum = shareNum;
    }


    public int getCollectNum() {
        return collectNum;
    }

    public void setCollectNum(int collectNum) {
        this.collectNum = collectNum;
    }

    public int getReportNum() {
        return reportNum;
    }

    public void setReportNum(int reportNum) {
        this.reportNum = reportNum;
    }


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

    public String getCircleid() {
        return circleid;
    }

    public void setCircleid(String circleid) {
        this.circleid = circleid;
    }

    public String getBackPic() {
        return backPic;
    }

    public void setBackPic(String backPic) {
        this.backPic = backPic;
    }

    public LinkedHashSet<String> getPhotos() {
        return photos;
    }

    public void setPhotos(LinkedHashSet<String> photos) {
        this.photos = photos;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public LinkedHashSet<String> getStartTime() {
        return startTime;
    }

    public void setStartTime(LinkedHashSet<String> startTime) {
        this.startTime = startTime;
    }

    public int getAddrType() {
        return addrType;
    }

    public void setAddrType(int addrType) {
        this.addrType = addrType;
    }

    public String getAddrInfo() {
        return addrInfo;
    }

    public void setAddrInfo(String addrInfo) {
        this.addrInfo = addrInfo;
    }

    public double[] getPosition() {
        return position;
    }

    public void setPosition(double[] position) {
        this.position = position;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public int getPayOrFree() {
        return payOrFree;
    }

    public void setPayOrFree(int payOrFree) {
        this.payOrFree = payOrFree;
    }

    public double getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(double payAmount) {
        this.payAmount = payAmount;
    }

    public String getPayName() {
        return payName;
    }

    public void setPayName(String payName) {
        this.payName = payName;
    }

    public String getPayInfo() {
        return payInfo;
    }

    public void setPayInfo(String payInfo) {
        this.payInfo = payInfo;
    }

    public int getAppointment() {
        return appointment;
    }

    public void setAppointment(int appointment) {
        this.appointment = appointment;
    }

    public int getUserLimit() {
        return userLimit;
    }

    public void setUserLimit(int userLimit) {
        this.userLimit = userLimit;
    }

    public boolean isPhone() {
        return isPhone;
    }

    public void setPhone(boolean phone) {
        isPhone = phone;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getChatroomid() {
        return chatroomid;
    }

    public void setChatroomid(String chatroomid) {
        this.chatroomid = chatroomid;
    }

    public String getVideoPic() {
        return videoPic;
    }

    public void setVideoPic(String videoPic) {
        this.videoPic = videoPic;
    }

    public int getPartyUserNum() {
        return partyUserNum;
    }

    public void setPartyUserNum(int partyUserNum) {
        this.partyUserNum = partyUserNum;
    }
}
