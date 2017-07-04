package com.lad.vo;

import java.util.HashSet;

/**
 * 功能描述：
 * Version: 1.0
 * Time:2017/6/25
 */
public class NoteVo extends BaseVo {

    private String nodeid;

    private String subject;

    private String content;

    private Long visitCount;

    private Long commontCount;

    private Long transCount;

    private HashSet<String> photos = new HashSet<>();

    private String createuid;

    private double[] position;
    private String circleId;

    private boolean isForward = false;

    public String getNodeid() {
        return nodeid;
    }

    public void setNodeid(String nodeid) {
        this.nodeid = nodeid;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(Long visitCount) {
        this.visitCount = visitCount;
    }

    public Long getCommontCount() {
        return commontCount;
    }

    public void setCommontCount(Long commontCount) {
        this.commontCount = commontCount;
    }

    public Long getTransCount() {
        return transCount;
    }

    public void setTransCount(Long transCount) {
        this.transCount = transCount;
    }

    public String getCreateuid() {
        return createuid;
    }

    public void setCreateuid(String createuid) {
        this.createuid = createuid;
    }

    public boolean isForward() {
        return isForward;
    }

    public void setForward(boolean forward) {
        isForward = forward;
    }

    public HashSet<String> getPhotos() {
        return photos;
    }

    public void setPhotos(HashSet<String> photos) {
        this.photos = photos;
    }

    public double[] getPosition() {
        return position;
    }

    public void setPosition(double[] position) {
        this.position = position;
    }

    public String getCircleId() {
        return circleId;
    }

    public void setCircleId(String circleId) {
        this.circleId = circleId;
    }
}
