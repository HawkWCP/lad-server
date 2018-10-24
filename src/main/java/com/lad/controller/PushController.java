package com.lad.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.lad.bo.PushTokenBo;
import com.lad.bo.UserBo;
import com.lad.service.ITokenService;
import com.lad.service.IUserService;

@RestController
@RequestMapping("push")
public class PushController extends BaseContorller {
	private Logger logger = LogManager.getLogger();

	@Autowired
	private IUserService userService;

	@Autowired
	private ITokenService tokenService;

	@PostMapping("/huaweiToken")
	public String saveHuaweiToken(String userId, String token) {
		logger.info("@PostMapping(\"/huaweiToken\")=====userId:{},token:{}", userId, token);
		Map<String, Object> map = new HashMap<>();
		try {
			if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(token)) {
				map.put("ret", -1);
				map.put("message", "参数错误");
				return JSON.toJSONString(map);
			}
			UserBo userBo = userService.getUser(userId);
			if (userBo == null) {
				map.put("ret", -1);
				map.put("message", "userId无效");
				return JSON.toJSONString(map);
			}

			PushTokenBo tokenBo = tokenService.findTokenByUserId(userId);
			if (tokenBo != null) {
				if (!token.equals(tokenBo.getHuaweiToken())) {
					tokenBo.setHuaweiToken(token);
					tokenService.updateHuaweiToken(tokenBo);
				}
			} else {
				tokenBo = new PushTokenBo();
				tokenBo.setHuaweiToken(token);
				tokenBo.setUserId(userId);
				tokenBo = tokenService.insert(tokenBo);
			}
		} catch (Exception e) {
			logger.error("@PostMapping(\"/huaweiToken\")=====error:{}", e);
			map.put("ret", -1);
			map.put("message", "服务器发生错误:"+e.toString());
			return JSON.toJSONString(map);
		}
		map.put("ret", 0);
		map.put("result", "token保存成功");
		return JSON.toJSONString(map);
	}
}
