package com.lad.vo;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2017
 */
public class UserCircleVo extends UserBaseVo {
    @Override
    public boolean isStar() {
        return star;
    }

    @Override
    public void setStar(boolean star) {
        this.star = star;
    }

    private boolean star;
    private int hasCircleNum;

    private int maxCircleNum;

    private String backName;

    public int getHasCircleNum() {
        return hasCircleNum;
    }

    public void setHasCircleNum(int hasCircleNum) {
        this.hasCircleNum = hasCircleNum;
    }

    public int getMaxCircleNum() {
        return maxCircleNum;
    }

    public void setMaxCircleNum(int maxCircleNum) {
        this.maxCircleNum = maxCircleNum;
    }

    public String getBackName() {
        return backName;
    }

    public void setBackName(String backName) {
        this.backName = backName;
    }
}
