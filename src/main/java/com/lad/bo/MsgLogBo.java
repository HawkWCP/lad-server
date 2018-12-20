package com.lad.bo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection = "msg_log")
@SuppressWarnings("serial")
public class MsgLogBo extends BaseBo {
	@Id
	private String id;
    private String channel;
    private String msg;	
    private String ts;		
    private String msgid;;
    private String uuid;	
}
