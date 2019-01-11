package com.lad.bo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


// 实体在后续修改中应该废弃掉,所有关注有关功能应当统一使用CareBo
@Setter
@Getter
@ToString
@Document(collection = "careAndPass")
@SuppressWarnings("serial")
public class CareAndPassBo extends BaseBo {
	// 责任主体id
	private String mainId;
	
	// 被动实体id集合
	private Map<String,Set<String>> careRoster = new HashMap<String,Set<String>>(); // 感兴趣的人,保存id的list;
	// 拉黑
	private Set<String> passRoster = new HashSet<>();	// 不感兴趣的人,保存id的list;
			
	// 是什么情境下进行的拉黑 1. 找儿媳;2. 找老伴;3. 找驴友
	private String situation;
}
