package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * ����������Ȧ�ӷ�����ʷ
 * Version: 1.0
 * Time:2017/8/16
 */
@Document(collection = "circleHistory")
public class CircleHistoryBo extends BaseBo {

    private String userid;

    private String circleid;

    private double[] position;
    //���ʼ�¼���ͣ� 0Ȧ�ӣ� 1 ����
    private int type;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getCircleid() {
        return circleid;
    }

    public void setCircleid(String circleid) {
        this.circleid = circleid;
    }

    public double[] getPosition() {
        return position;
    }

    public void setPosition(double[] position) {
        this.position = position;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
