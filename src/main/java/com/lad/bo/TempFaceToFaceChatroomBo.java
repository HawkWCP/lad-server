package com.lad.bo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "tempFaceToFaceChatroom")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class TempFaceToFaceChatroomBo extends BaseBo {

	private String seq;
	private double[] position;
	private String userid;
}
