package com.lad.bo;

import java.util.Date;
import java.util.LinkedHashSet;
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

	private String subject;
	private String content;
	private LinkedList<String> photos = new LinkedList<>();
	private String landmark;
	private double[] position;
	private String circleId;
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

	// 精华 管理员操作
	private int essence;
	// 置顶 管理员操作
	private int top;

	// 上传的文件类型，前端传值
	private String type;

	// 视频缩略图
	private String videoPic;

	// 热门数
	private double temp;

	// 是否同步个人动态
	private boolean isAsync;

	// 0 原创 ， 1转发
	private int forward;
	// 转发的原帖子id
	private String sourceid;
	// 帖子中@的用户
	private LinkedList<String> atUsers;

	// 0 表示帖子， 1表示资讯, 2.表示养老院, 3.表示招接演出
	private int noteType;
	// 资讯类型
	private int inforType;
	// 来源资讯类型名称
	private String inforTypeName;
	// 演出来源类型
	private int showType;
	private String forwardUsers;

	// 转发 时 前面涉及的所有noteid
	private LinkedHashSet<String> preNoteids;
	// 转发时的评论
	private String view;

	// 发布日期
	private String createDate;
	private Date topUpdateTime;
	private Date essUpdateTime;
}
