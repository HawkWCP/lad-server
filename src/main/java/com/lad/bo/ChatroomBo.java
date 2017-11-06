package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashSet;

@Document(collection = "chatroom")
public class ChatroomBo extends BaseBo {

	private String name;
	private LinkedHashSet<String> users = new LinkedHashSet<>();
	//1 表示一对一聊天室，2表示群聊，3表示面对面建群
	private int type;
	private String userid;
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


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LinkedHashSet<String> getUsers() {
		return users;
	}

	public void setUsers(LinkedHashSet<String> users) {
		this.users = users;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getFriendid() {
		return friendid;
	}

	public void setFriendid(String friendid) {
		this.friendid = friendid;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public int getExpire() {
		return expire;
	}

	public void setExpire(int expire) {
		this.expire = expire;
	}

	public double[] getPosition() {
		return position;
	}

	public void setPosition(double[] postion) {
		this.position = postion;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isOpen() {
		return isOpen;
	}

	public void setOpen(boolean open) {
		isOpen = open;
	}

	public boolean isVerify() {
		return isVerify;
	}

	public void setVerify(boolean verify) {
		isVerify = verify;
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}
}

