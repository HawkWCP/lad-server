package com.lad.bo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "token")
@Setter
@Getter
@ToString
public class PushTokenBo {
	public static final int TOKEN_ENABLE = 1;
	public static final int TOKEN_CLOSE = 2;
	
	
	@Id
	private String id;
	private String userId;
	private String huaweiToken;
	private String xiaomiAlias;
	// 1:启动;2:关闭
	private int status; 
}
