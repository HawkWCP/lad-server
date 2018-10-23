package com.lad.vo;

import java.util.HashSet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("all")
public class CircleVo extends BaseVo {
	/*=====基础资料=====*/
	private String id;
	private String tag;
	private String sub_tag;	
	private String name;
	private String description;
	private String createuid;
	private String headPicture;
	private HashSet<String> users = new HashSet<String>();
	private HashSet<String> usersApply = new HashSet<String>();
	private HashSet<String> usersRefuse = new HashSet<String>();
	private HashSet<String> notes = new HashSet<String>();
	
	/*=====位置信息=====*/
	private double[] position;	// 坐标
	private String province;	// 省
	private String city;		// 市
	private String district;	// 区
	
	/*=====圈子设置=====*/
	private boolean isOpen;		// 是否公开
	private boolean isVerify;	// 是否需要验证
	private boolean takeShow;	// 是否承接演出


	/*=====圈子公告=====*/
	private String noticeTitle;	// 公告标题
	private String notice;		// 公告内容

	/*=====用户个别设置返回=====*/
	private int top = 0;		// 是否指定
	private int userAdd;		// 是否加入
	private int unReadNum; 		// 未读数

	/*=====统计学信息=====*/
	private long notesSize;		// 帖子数
	private int usersSize;		// 用户数
	private long partyNum;		// 聚会数
	private long visitNum;		// 主页访问量
}
