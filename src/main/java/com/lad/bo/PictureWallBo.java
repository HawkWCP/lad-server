package com.lad.bo;

import java.util.LinkedList;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection = "pictureWall")
public class PictureWallBo extends BaseBo {
	private LinkedList<String> pictures = new LinkedList<>();
}
