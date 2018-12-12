package com.lad.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.lad.service.IUserService;
import com.lad.util.VivoPushUtil;
import com.mongodb.WriteResult;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

@RestController
@RequestMapping("push")
public class PushController extends BaseContorller {
	private Logger logger = LogManager.getLogger();

	@Autowired
	private IUserService userService;

	@Deprecated
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

			// 查找当前用户关联的token
			PushTokenBo tokenBo = tokenService.findTokenByUserId(userId);
			
			if (tokenBo != null) {
				// 如果存在当前token,将状态修改为1
				if (!token.equals(tokenBo.getHuaweiToken())) {
					// 如果已存在的token值与传入的token不符合,则修改数据库的token值
					tokenBo.setHuaweiToken(token);
				}				
				tokenBo.setStatus(1);
				tokenService.updateHuaweiToken(tokenBo);
			} else {
				tokenBo = new PushTokenBo();
				tokenBo.setHuaweiToken(token);
				tokenBo.setUserId(userId);
				tokenBo.setStatus(1);
				tokenBo = tokenService.insert(tokenBo);
			}
			
			// 将其他用户的token的状态设置为2
			WriteResult result = tokenService.updateOtherStatus(token,userId);

		} catch (Exception e) {
			logger.error("@PostMapping(\"/huaweiToken\")=====error:{}", e);
			map.put("ret", -1);
			map.put("message", "服务器发生错误:" + e.toString());
			return JSON.toJSONString(map);
		}
		map.put("ret", 0);
		map.put("result", "token保存成功");
		return JSON.toJSONString(map);
	}
	
	
	@ApiOperation("注册或启动华为token")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "userId", value = "用户id", required = true, paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "token", value = "用户token", paramType = "query", required = true, dataType = "string") })
	@PostMapping("/token-enable-huawei")
	public String enableHuaweiToken(String userId, String token) {
		logger.info("@PostMapping(\"/token-enable-huawei\")=====userId:{},token:{}", userId, token);
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

			// 查找当前用户关联的token
			PushTokenBo tokenBo = tokenService.findTokenByUserId(userId);
			
			if (tokenBo != null) {
				// 如果存在当前token,将状态修改为1
				if (!token.equals(tokenBo.getHuaweiToken())) {
					// 如果已存在的token值与传入的token不符合,则修改数据库的token值
					tokenBo.setHuaweiToken(token);
				}				
				tokenBo.setStatus(1);
				tokenService.updateHuaweiToken(tokenBo);
			} else {
				tokenBo = new PushTokenBo();
				tokenBo.setHuaweiToken(token);
				tokenBo.setUserId(userId);
				tokenBo.setStatus(1);
				tokenBo = tokenService.insert(tokenBo);
			}
			
			// 将其他用户的token的状态设置为2
			WriteResult result = tokenService.updateOtherStatus(token,userId);
			map.put("ret", 0);
			map.put("result", "token保存成功");

		} catch (Exception e) {
			logger.error("@PostMapping(\"/huaweiToken\")=====error:{}", e);
			map.put("ret", -1);
			map.put("message", "服务器发生错误:" + e.toString());
			return JSON.toJSONString(map);
		}
		return JSON.toJSONString(map);
	}
	
	
	@ApiOperation("关闭华为token")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "userId", value = "用户id", required = true, paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "token", value = "用户token", paramType = "query", required = true, dataType = "string") })
	@PostMapping("/token-close-huawei")
	public String closeHuaweiToken(String userId, String token) {
		logger.info("@PostMapping(\"/token-close-huawei\")=====userId:{},token:{}", userId, token);
		Map<String, Object> map = new HashMap<>();
		try {
			if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(token)) {
				map.put("ret", -1);
				map.put("message", "参数错误");
				return JSON.toJSONString(map);
			}
			
			PushTokenBo tokenBo = tokenService.findTokenByUserIdAndToken(userId,token);
			if(tokenBo == null) {
				map.put("ret", -1);
				map.put("message", "请求关闭的token不存在或未启用");
				return JSON.toJSONString(map);			
			}
			
			WriteResult result = tokenService.closeTokenByUseridAndToken(userId,token);
			map.put("ret", 0);
			map.put("result", "token关闭成功");
		} catch (Exception e) {
			logger.error("@PostMapping(\"/huaweiToken\")=====error:{}", e);
			map.put("ret", -1);
			map.put("message", "服务器发生错误:" + e.toString());
			return JSON.toJSONString(map);
		}
		return JSON.toJSONString(map);
	}

	@PostMapping("/huawei-token-del")
	public String deletedHuaweiToken(String userId) {
		Map<String, Object> map = new HashMap<>();
		try {
			PushTokenBo userTocken = tokenService.findTokenByUserId(userId);
			if (userTocken == null) {
				map.put("ret", -1);
				map.put("message", "用户id错误或为注册token");
				return JSON.toJSONString(map);
			}
			tokenService.deletedTokenByTokenAndUserId(userTocken.getHuaweiToken(), userId);
			map.put("ret", 0);
			map.put("result", "OK");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return JSON.toJSONString(map);
	}
	
	@PostMapping("vivo")
	public void oppoPush() {
		List<String> list = new ArrayList<>();
		list.add("59ef501131f0a54720f189b3");

		VivoPushUtil.sendToMany("标题---vivo", "内容---vivo", list, "http://www.vivo.com");
	}
}
