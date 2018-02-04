package com.lad.controller;

import com.lad.bo.UserBo;
import com.lad.service.IFriendsService;
import com.lad.service.IUserService;
import com.lad.util.CommonUtil;
import com.lad.util.Constant;
import com.lad.util.ERRORCODE;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("upload")
public class UploadController extends BaseContorller {

	private static Logger logger = LogManager.getLogger(UploadController.class);

	@Autowired
	private IUserService userService;

	@Autowired
	private IFriendsService friendsService;

	@PostMapping("/head-picture")
	public String head_picture(@RequestParam("head_picture") MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) {
		HttpSession session = request.getSession();
		if (session.isNew()) {
			return "{\"ret\":20002,\"error\":\"未登录\"}";
		}
		if (session.getAttribute("isLogin") == null) {
			return "{\"ret\":20002,\"error\":\"未登录\"}";
		}
		UserBo userBo = (UserBo) session.getAttribute("userBo");
		if (userBo == null) {
			return "{\"ret\":20002,\"error\":\"未登录\"}";
		}
		userBo = userService.getUser(userBo.getId());
		String userId = userBo.getId();
		long time = Calendar.getInstance().getTimeInMillis();
		String fileName = String.format("%s-%d-%s", userId, time, file.getOriginalFilename());
		String path = CommonUtil.upload(file, Constant.HEAD_PICTURE_PATH, fileName, 0);
		userBo.setHeadPictureName(path);
		userService.updateHeadPictureName(userBo);
		updateUserHeadPic(userId, path);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("path", path);
		return JSONObject.fromObject(map).toString();
	}

	@Async
	private void updateUserHeadPic(String userid, String headPic){
		friendsService.updateUsernameByFriend(userid, "", headPic);
	}

	@PostMapping("/feedback-picture")
	public String feedback_picture(@RequestParam("feedback_picture") MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) {
		HttpSession session = request.getSession();
		if (session.isNew()) {
			return "{\"ret\":20002,\"error\":\"未登录\"}";
		}
		if (session.getAttribute("isLogin") == null) {
			return "{\"ret\":20002,\"error\":\"未登录\"}";
		}
		UserBo userBo = (UserBo) session.getAttribute("userBo");
		if (userBo == null) {
			return "{\"ret\":20002,\"error\":\"未登录\"}";
		}
		userBo = userService.getUser(userBo.getId());
		String userId = userBo.getId();
		long time = Calendar.getInstance().getTimeInMillis();
		String fileName = String.format("%s-%d-%s", userId, time, file.getOriginalFilename());
		String path = CommonUtil.upload(file, Constant.FEEDBACK_PICTURE_PATH, fileName, 0);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ret", 0);
		map.put("path", path);
		return JSONObject.fromObject(map).toString();
	}
	
	@PostMapping("/imfile")
	public String imfile(MultipartFile imfile, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(), ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		if (imfile != null) {
			long time = Calendar.getInstance().getTimeInMillis();
			String fileName = String.format("%s-%d-%s",userBo.getId(), time, imfile.getOriginalFilename());
			logger.info("===== start upload  imfile name : {}" , fileName);
			String path = CommonUtil.upload(imfile, Constant.IMFILE_PATH, fileName, 0);
			logger.info("===== end upload  imfile path : {}, update time : {}" , path,
					(System.currentTimeMillis()- time));
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ret", 0);
			map.put("path", path);
			return JSONObject.fromObject(map).toString();
		}
		return Constant.COM_FAIL_RESP;
	}

}
