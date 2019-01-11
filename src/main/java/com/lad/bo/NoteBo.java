package com.lad.bo;

import java.util.Date;
import java.util.LinkedList;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Document(collection = "note")

@ToString
@SuppressWarnings("serial")
public class NoteBo extends BaseBo {
	// 从其他帖子转发的帖子
	public static final int NOTE_FORWARD = 0;
	// 从资讯转发的帖子
	public static final int INFOR_FORWARD = 1;
	// 从养老院转发的帖子
	public static final int REST_FORWARD = 2;
	// 从演出转发的帖子
	public static final int SHOW_FORWARD = 3;

	/*============================基本数据========================*/
	// 标题
	private String subject;
	// 内容
	private String content;
	// 照片
	private LinkedList<String> photos = new LinkedList<>();
	// 地标
	private String landmark;
	// 坐标
	private double[] position;
	// 上传的文件类型
	private String type;
	// 视频缩略图
	private String videoPic;
	
	/*============================操作数据========================*/
	// 归属圈子id
	private String circleId;
	// 是否是精华帖,管理员或圈主有操作权限
	private int essence;
	// 是否制定,管理员或圈主有操作权限
	private int top;
	// 置顶时间
	private Date topUpdateTime;
	// 加精时间
	private Date essUpdateTime;
	// 是否同步个人动态
	private boolean isAsync;

	/*============================转发有关=========================*/
	// 0 原创 ， 1转发
	private int forward;
	// 转发的原帖子id
	private String sourceid;
	// 帖子中@的用户
	private LinkedList<String> atUsers;
	// 帖子的类型,见本类常量
	private int noteType;
	// 资讯类型,如果noteType显示该帖子转发自资讯,则设置该字段
	private int inforType;
	// 来源资讯类型名称
	private String inforTypeName;
	// 演出来源类型,如果noteType显示该帖子转发自演出,则设置该字段
	private int showType;
	// 转发者
	private String forwardUsers;
	// 转发时的评论
	private String view;
	// 转发时间
	private String createDate;

	
	/*=========================动态数据=========================*/
	// 访问量
	private long visitcount;
	// 转发量
	private long transcount;
	// 评论数
	private long commentcount;
	// 点赞数
	private long thumpsubcount;
	// 点赞数
	private long collectcount;
	// 热门数
	private double temp;
}
