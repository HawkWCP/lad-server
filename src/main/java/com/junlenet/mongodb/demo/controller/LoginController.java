package com.junlenet.mongodb.demo.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.junlenet.mongodb.demo.service.ILoginService;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("login")
public class LoginController extends BaseContorller {

	@Autowired
	private ILoginService loginService;

	@RequestMapping("/verification-send")
	@ResponseBody
	public String verification_send(String phone, HttpServletRequest request, HttpServletResponse response) {
		if (!StringUtils.hasLength(phone)) {
			return "{\"ret\":-1,\"error\":\"error phone\"}";
		}
		Map<String, Object> map = new HashMap<String, Object>();
		HttpSession session = request.getSession();
		session.setAttribute("phone", phone);
		session.setAttribute("verification", "111111");
		map.put("ret", 0);
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/login-quick")
	@ResponseBody
	public String login_quick(String phone, String verification, HttpServletRequest request,
			HttpServletResponse response) {
		HttpSession session = request.getSession();
		if(session.isNew()){
			return "{\"ret\":-1,\"error\":\"session is null\"}";
		}
		if (!StringUtils.hasLength(phone)) {
			return "{\"ret\":-1,\"error\":\"error phone\"}";
		}
		if (!StringUtils.hasLength(verification)) {
			return "{\"ret\":-1,\"error\":\"verification is null\"}";
		}
		String verification_session = (String) session.getAttribute("verification");
		if(session.getAttribute("phone") == null){
			return "{\"ret\":-1,\"error\":\"error session\"}";
		}
		String phone_session = (String) session.getAttribute("phone");
		Map<String, Object> map = new HashMap<String, Object>();
		if (verification_session.equals(verification) && phone_session.equals(phone)) {
			map.put("ret", 0);
			session.setAttribute("isLogin", true);
		} else {
			map.put("ret", -1);
			map.put("error", "verification error");
			session.setAttribute("isLogin", false);
		}
		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/login")
	@ResponseBody
	public String login(String phone, String password, HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		if (!StringUtils.hasLength(phone)) {
			return "{\"ret\":-1,\"error\":\"error userName\"}";
		}
		if (!StringUtils.hasLength(password)) {
			return "{\"ret\":-1,\"error\":\"error password\"}";
		}
		Map<String, Object> map = new HashMap<String, Object>();
		if (loginService.getUser(phone, password) != null) {
			map.put("ret", 0);
			session.setAttribute("isLogin", true);
			session.setAttribute("userBo", loginService.getUser(phone, password));
		} else {
			map.put("ret", -1);
			map.put("error", "username or password is wrong");
			session.setAttribute("isLogin", true);
		}
		return JSONObject.fromObject(map).toString();
	}

	
}
