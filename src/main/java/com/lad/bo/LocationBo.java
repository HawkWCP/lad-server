package com.lad.bo;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "location")
@Setter
@Getter
@ToString
@SuppressWarnings("serial")
public class LocationBo extends BaseBo {

	private String userid;
	private double[] position = new double[2];

	public LocationBo() {
	}

	public LocationBo(String userid, double x, double py) {
		this.userid = userid;
		this.position[0] = x;
		this.position[1] = py;
	}

	@PersistenceConstructor
	public LocationBo(String userid, double[] position) {
		this.userid = userid;
		this.position = position;
	}

}