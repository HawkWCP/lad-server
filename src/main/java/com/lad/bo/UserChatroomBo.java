package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述： 用户申请加入聊天室信息 Copyright: Copyright (c) 2017 Version: 1.0 Time:2017/12/24
 */

@Document(collection = "userChatroom")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class UserChatroomBo extends BaseBo {

	private String userid;

	private String chatroomid;
	// 0申请加入， 1 同意， -1拒绝
	private int status;

	private String reason;

	private String refuse;
}
