package com.lad.bo;

import java.util.HashSet;
import java.util.LinkedList;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "friends")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class FriendsBo extends BaseBo {
	//主用户
	private String userid;
	//好友用户
	private String friendid;
	private int apply;//0：申请；1：同意好友；-1拒绝好友
	private String backname;
	private HashSet<String> phone = new HashSet<>();
	private String description;
	private Integer VIP = 0;
	private Integer black = 0;
	private String username;
	private String friendHeadPic;
	private String chatroomid;
	//关联账号的角色，true 表示当前主用户角色是父母，false表示当前主用户角色是子女
	private boolean parent;
	//关联账号状态，0表示普通好友， 1 表示发送关联申请，2 表示被申请用户的状态， 3表示已建立关联， -1  表示拒绝或取消
	private int relateStatus = 0;
	private LinkedList<String> usedBackName = new LinkedList<>();
}
