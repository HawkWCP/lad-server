package com.lad.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class MsgLogVo {
	private ChatroomVo chatRoom;
	private List<MsgBaseVo> file;
	private List<MsgBaseVo> word;
}
