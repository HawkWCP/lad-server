package com.lad.bo;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：城市实体类 Copyright: Copyright (c) 2017 Version: 1.0 Time:2017/9/5
 */
@Document(collection = "city")
@SuppressWarnings("serial")
@Getter
@Setter
@ToString
public class CityBo implements Serializable {

	@Id
	private String id;

	private String province;

	private String city;

	private String distrit;

}
