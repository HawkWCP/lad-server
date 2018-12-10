package com.lad.vo;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MsgBaseVo {
    private String channel;
    private String msg;	
    private Date sendTime;		
    private UserBaseVo sender;
}
