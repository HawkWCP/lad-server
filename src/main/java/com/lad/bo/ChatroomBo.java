package com.lad.bo;

import java.util.LinkedHashSet;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chatroom")
@SuppressWarnings("serial")
@Getter
@Setter
@ToString
public class ChatroomBo extends BaseBo {
	//聊天室名
	private String name;
	//聊天室用户
	private LinkedHashSet<String> users = new LinkedHashSet<>();
	//1 表示一对一聊天室，2表示群聊，3表示面对面建群 , 4表示聚会临时聊天 , 5 非聚会临时聊天
	private int type;
	//用户id
	private String userid;
	//好友id
	private String friendid;
	private int seq;
	private int expire = 1;

	private double[] position;

	private String description;
	//是否允许加入
	private boolean isOpen = true;
	//圈子加入是否需要校验
	private boolean isVerify;

	private String master;

	private boolean isNameSet;
	
	//聚会id
	private String targetid;
}

