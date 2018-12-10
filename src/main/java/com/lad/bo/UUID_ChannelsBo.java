package com.lad.bo;

import java.util.LinkedHashSet;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "uuid_channels")
@Setter
@Getter
@ToString
public class UUID_ChannelsBo {
	private String id;
	private LinkedHashSet<String> channels;
}
