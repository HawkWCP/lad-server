package com.lad.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.lad.bo.RestHomeBo;
import com.lad.bo.RetiredPeopleBo;
import com.lad.bo.UserBo;
import com.lad.service.IRestHomeService;
import com.lad.service.IUserService;
import com.lad.util.CommonUtil;
import com.lad.util.Constant;
import com.lad.util.ERRORCODE;
import com.lad.vo.RestHomeVo;
import com.lad.vo.RetiredPeopleVo;
import com.mongodb.WriteResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

@Api("养老院")
@RestController
@RequestMapping("resthome")
@SuppressWarnings("all")
public class RestHomeController extends BaseContorller {

	private static final Logger logger = LogManager.getLogger(RestHomeController.class);

	@Autowired
	private IRestHomeService restHomeService;
	@Autowired
	private IUserService userService;




//	@ApiOperation("养老院列表")
	@ApiOperation("推荐养老院")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "peopleId", value = "养老院id", required = true, paramType = "query", dataType = "String"),
			@ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "显示条数", required = true, paramType = "query", dataType = "int") })
	@PostMapping("/home-list")
	public String homeList(int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/home-list\")=====page:{},limit:{}", page, limit);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}

			List<RestHomeBo> restHome = restHomeService.findHomeListByUid(page, limit);
			List<RestHomeVo> resultList = new ArrayList<>();
			if(restHome!=null && restHome.size()>0) {
				homeBo2Vo(restHome, resultList);
			}

			map.put("ret", 0);
			map.put("result", resultList);
			
		} catch (Exception e) {
			logger.error("@PostMapping(\\\"/recommend-home\\\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误");
		}

		return JSON.toJSONString(map);
	}
	
	@ApiOperation("推荐养老院")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "peopleId", value = "养老院id", required = true, paramType = "query", dataType = "String"),
			@ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "显示条数", required = true, paramType = "query", dataType = "int") })
	@PostMapping("/recommend-home")
	public String recommendHome(String peopleId, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/recommend-home\")=====peopleId:{},page:{},limit:{}", peopleId, page, limit);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}

			RetiredPeopleBo peopleBo = restHomeService.findPeopleById(peopleId);
			if(peopleBo == null) {
				map.put("ret", -1);
				map.put("message", "老人信息不存在或已删除");
				return JSON.toJSONString(map);
			}

			String homeArea = CommonUtil.getCity(peopleBo.getHomeArea());
			String wannaArea = CommonUtil.getCity(peopleBo.getWannaArea());
			
			List<RestHomeBo> recommendBo = restHomeService.findRecommendHome(homeArea,wannaArea,page,limit);
			List<RestHomeVo> resultList = new ArrayList<>();
			if(recommendBo!=null && recommendBo.size()>0) {
				homeBo2Vo(recommendBo, resultList);
			}

			map.put("ret", 0);
			map.put("result", resultList);
			
		} catch (Exception e) {
			logger.error("@PostMapping(\\\"/recommend-home\\\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误");
		}

		return JSON.toJSONString(map);
	}
	
	
	// TODO 未测试
	@ApiOperation("推荐住院人") 
	@ApiImplicitParams({
			@ApiImplicitParam(name = "homeId", value = "养老院id", required = true, paramType = "query", dataType = "String"),
			@ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "显示条数", required = true, paramType = "query", dataType = "int") })
	@PostMapping("/recommend-people")
	public String recommendPeople(String homeId, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/recommend-people\")=====homeId:{},page:{},limit:{}", homeId, page, limit);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}

			RestHomeBo homeBo = restHomeService.findHomeById(homeId);
			if(homeBo == null) {
				map.put("ret", -1);
				map.put("message", "养老院不存在或已删除");
				return JSON.toJSONString(map);
			}
			LinkedHashSet<String> knightService = homeBo.getKnightService();
			
			// 是否接受异地
			boolean acceptOtherArea = knightService.contains(Constant.YL_TS_ED);
			String area = CommonUtil.getCity(homeBo.getArea());
			
			List<RetiredPeopleBo> recommendBo = restHomeService.findRecommendPeople(area,acceptOtherArea,page,limit);
			List<RetiredPeopleVo> resultList = new ArrayList<>();
			if(recommendBo!=null && recommendBo.size()>0) {
				peopleBo2Vo(recommendBo, resultList);

			}
			map.put("ret", 0);
			map.put("result", resultList);

		} catch (Exception e) {
			logger.error("@PostMapping(\"/recommend-people\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误");
		}

		return JSON.toJSONString(map);
	}


	@ApiOperation("我发布的养老院信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "显示条数", required = true, paramType = "query", dataType = "int") })
	@PostMapping("/my-home-publish")
	public String myHomePublish(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/my-publish\")=====page:{},limit:{}", page, limit);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}

			List<RestHomeBo> publishes = restHomeService.findHomeListByUid(userBo.getId(), page, limit);
			List<RestHomeVo> resultList = new ArrayList<>();
			homeBo2Vo(publishes, resultList);

			map.put("ret", 0);
			map.put("result", resultList);

		} catch (Exception e) {
			logger.error("@PostMapping(\"/my-publish\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误");
		}

		return JSON.toJSONString(map);
	}

	@ApiOperation("我的发布")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "显示条数", required = true, paramType = "query", dataType = "int") })
	@PostMapping("/my-people-publish")
	public String myPeoplePublish(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/my-publish\")=====page:{},limit:{}", page, limit);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}

			List<RetiredPeopleBo> publishes = restHomeService.findPeopleListByUid(userBo.getId(), page, limit);
			List<RetiredPeopleVo> resultList = new ArrayList<>();
			peopleBo2Vo(publishes, resultList);

			map.put("ret", 0);
			map.put("result", resultList);

		} catch (Exception e) {
			logger.error("@PostMapping(\"/my-publish\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误");
		}

		return JSON.toJSONString(map);
	}

	@ApiOperation("修改养老信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "homeJson", value = "养老院json数据", required = true, paramType = "query", dataType = "string") })
	@PostMapping("/updateHome")
	public String updateHome(String homeJson, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/updateHome\")=====homeJson:{}", homeJson);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}

			if (StringUtils.isEmpty(homeJson)) {
				map.put("ret", -1);
				map.put("message", "参数错误");
				return JSON.toJSONString(map);
			}

			RestHomeBo homeBo = JSON.parseObject(homeJson, RestHomeBo.class);
			if (homeBo.getId() == null) {
				map.put("ret", -1);
				map.put("message", "缺少id");
				return JSON.toJSONString(map);
			}
			RestHomeBo resultBo = restHomeService.findHomeById(homeBo.getId());
			if (resultBo == null) {
				map.put("ret", -1);
				map.put("message", "数据不存在或已删除");
				return JSON.toJSONString(map);
			}

			Map<String, Object> params = isUpdate(homeJson, resultBo);

			WriteResult result = restHomeService.updateHomeById(homeBo.getId(), params);
			if (result.isUpdateOfExisting()) {
				map.put("ret", 0);
				map.put("message", "修改成功");
			} else {
				map.put("ret", -1);
				map.put("message", "修改失败");
			}
		} catch (com.alibaba.fastjson.JSONException e) {
			logger.error("@PostMapping(\"/updateHome\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "JSON格式错误");
		} catch (JSONException e) {
			logger.error("@PostMapping(\"/updateHome\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "JSON格式错误");
		} catch (Exception e) {
			logger.error("@PostMapping(\"/updateHome\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误");
		}

		return JSON.toJSONString(map);
	}

	@ApiOperation("修改养老者信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "peopleJson", value = "养老者id", required = true, paramType = "query", dataType = "string") })
	@PostMapping("/updatePoeple")
	public String updatePoeple(String peopleJson, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/updatePoeple\")=====peopleJson:{}", peopleJson);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}

			if (StringUtils.isEmpty(peopleJson)) {
				map.put("ret", -1);
				map.put("message", "参数错误");
			}

			RetiredPeopleBo poepleBo = JSON.parseObject(peopleJson, RetiredPeopleBo.class);
			if (poepleBo.getId() == null) {
				map.put("ret", -1);
				map.put("message", "缺少id");
			}
			RetiredPeopleBo resultBo = restHomeService.findPeopleById(poepleBo.getId());
			if (resultBo == null) {
				map.put("ret", -1);
				map.put("message", "数据不存在或已删除");
			}

			Map<String, Object> params = isUpdate(peopleJson, resultBo);

			WriteResult result = restHomeService.updatePeopleById(poepleBo.getId(), params);
			if (result.isUpdateOfExisting()) {
				map.put("ret", 0);
				map.put("message", "修改成功");
			} else {
				map.put("ret", -1);
				map.put("message", "修改失败");
			}

		} catch (com.alibaba.fastjson.JSONException e) {
			logger.error("@PostMapping(\"/updatePoeple\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "JSON格式错误");
		} catch (JSONException e) {
			logger.error("@PostMapping(\"/updatePoeple\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "JSON格式错误");
		} catch (Exception e) {
			logger.error("@PostMapping(\"/updatePoeple\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误");
		}

		return JSON.toJSONString(map);
	}

	@ApiOperation("住院者详情")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "peopleId", value = "住院者ID", required = true, paramType = "query", dataType = "string") })
	@PostMapping("/peopleInfo")
	public String peopleInfo(String peopleId, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/peopleInfo\")=====homeId:{}", peopleId);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}
			if (StringUtils.isEmpty(peopleId)) {
				map.put("ret", -1);
				map.put("message", "参数错误");
			}
			RetiredPeopleBo poepleBo = restHomeService.findPeopleById(peopleId);
			RetiredPeopleVo poepleVo = new RetiredPeopleVo();
			if (poepleBo != null) {
				peopleBo2Vo(poepleBo, poepleVo);
				map.put("ret", 0);
				map.put("result", poepleVo);
			} else {
				map.put("ret", -1);
				map.put("message", "数据不存在或已删除");
			}

		} catch (Exception e) {
			logger.error("@PostMapping(\"/peopleInfo\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误");
		}

		return JSON.toJSONString(map);
	}

	@ApiOperation("养老院详情")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "homeId", value = "养老院ID", required = true, paramType = "query", dataType = "string") })
	@PostMapping("/homeInfo")
	public String homeInfo(String homeId, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/homeInfo\")=====homeId:{}", homeId);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}
			if (StringUtils.isEmpty(homeId)) {
				map.put("ret", -1);
				map.put("message", "参数错误");
			}
			RestHomeBo homeBo = restHomeService.findHomeById(homeId);
			RestHomeVo homeVo = new RestHomeVo();
			if (homeBo != null) {
				homeBo2Vo(homeBo, homeVo);
				map.put("ret", 0);
				map.put("result", homeVo);
			} else {
				map.put("ret", -1);
				map.put("message", "数据不存在或已删除");
			}

		} catch (Exception e) {
			logger.error("@PostMapping(\"/homeInfo\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误");
		}

		return JSON.toJSONString(map);
	}

	@ApiOperation("删除住院者")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "peopleId", value = "住院者ID", required = true, paramType = "query", dataType = "string") })
	@PostMapping("/deletePeople")
	public String deletePeople(String peopleId, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/deletePeople\")=====peopleId:{}", peopleId);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}
			if (StringUtils.isEmpty(peopleId)) {
				map.put("ret", -1);
				map.put("message", "参数错误");
			}
			RetiredPeopleBo peopleBo = restHomeService.findPeopleById(peopleId);
			if (peopleBo != null && userBo.getId().equals(peopleBo.getCreateuid())) {
				WriteResult result = restHomeService.deletePeopleById(peopleId);
				if (result.isUpdateOfExisting()) {
					map.put("ret", 0);
					map.put("message", "删除成功");
				} else {
					map.put("ret", -1);
					map.put("message", "删除失败");
				}
			}

		} catch (Exception e) {
			logger.error("@PostMapping(\"/deletePeople\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误");
		}

		return JSON.toJSONString(map);
	}

	@ApiOperation("删除养老院")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "homeId", value = "养老院ID", required = true, paramType = "query", dataType = "string") })
	@PostMapping("/deleteHome")
	public String deleteHome(String homeId, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/deleteHome\")=====homeId:{}", homeId);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}
			if (StringUtils.isEmpty(homeId)) {
				map.put("ret", -1);
				map.put("message", "参数错误");
			}
			RestHomeBo homeBo = restHomeService.findHomeById(homeId);
			if (homeBo != null && userBo.getId().equals(homeBo.getCreateuid())) {
				WriteResult result = restHomeService.deleteHomeById(homeId);
				if (result.isUpdateOfExisting()) {
					map.put("ret", 0);
					map.put("message", "删除成功");
				} else {
					map.put("ret", -1);
					map.put("message", "删除失败");
				}
			}

		} catch (Exception e) {
			logger.error("@PostMapping(\"/deleteHome\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误");
		}

		return JSON.toJSONString(map);
	}

	@ApiOperation("添加养老院")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "resthomeJson", value = "养老院信息json数据", required = true, paramType = "query", dataType = "string") })
	@PostMapping("/inserthome")
	public String inserthome(String resthomeJson, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/inserthome\")=====resthomeJson:{}", resthomeJson);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}

			RestHomeBo homeBo = JSON.parseObject(resthomeJson, RestHomeBo.class);
			if (homeBo != null) {
				if (homeBo.isProtocol()) {
					homeBo.setCreateuid(userBo.getId());
					homeBo = restHomeService.inserthome(homeBo);
					map.put("ret", 0);
					map.put("homeid", homeBo.getId());
				} else {
					map.put("ret", -1);
					map.put("message", "请查看用户协议");
				}
			}
		} catch (com.alibaba.fastjson.JSONException e) {
			logger.error("@PostMapping(\"/inserthome\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "JSON格式错误");
		} catch (JSONException e) {
			logger.error("@PostMapping(\"/inserthome\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "JSON格式错误");
		} catch (Exception e) {
			logger.error("@PostMapping(\"/inserthome\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误");
		}

		return JSON.toJSONString(map);
	}

	@ApiOperation("添加养老信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "peopleJson", value = "养老信息json数据", required = true, paramType = "query", dataType = "string") })
	@PostMapping("/insertpoeple")
	public String insertPoeple(String peopleJson, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/insertpoeple\")=====peopleJson:{}", peopleJson);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}

			RetiredPeopleBo poepleBo = JSON.parseObject(peopleJson, RetiredPeopleBo.class);
			if (poepleBo != null) {
				if (poepleBo.isProtocol()) {
					poepleBo.setCreateuid(userBo.getId());
					poepleBo = restHomeService.inserthome(poepleBo);
					map.put("ret", 0);
					map.put("homeid", poepleBo.getId());
				} else {
					map.put("ret", -1);
					map.put("message", "请查看用户协议");
				}
			}
		} catch (com.alibaba.fastjson.JSONException e) {
			logger.error("@PostMapping(\"/insertpoeple\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "JSON格式错误");
		} catch (JSONException e) {
			logger.error("@PostMapping(\"/insertpoeple\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "JSON格式错误");
		} catch (Exception e) {
			logger.error("@PostMapping(\"/insertpoeple\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误");
		}

		return JSON.toJSONString(map);
	}

	private void homeBo2Vo(RestHomeBo homeBo, RestHomeVo homeVo) {
		if (homeBo != null && homeVo != null) {
			BeanUtils.copyProperties(homeBo, homeVo);
		}
	}

	private void homeBo2Vo(List<RestHomeBo> boList, List<RestHomeVo> voList) {
		for (RestHomeBo homeBo : boList) {
			RestHomeVo homeVo = new RestHomeVo();
			homeBo2Vo(homeBo, homeVo);
			voList.add(homeVo);
		}
	}

	private void peopleBo2Vo(RetiredPeopleBo poepleBo, RetiredPeopleVo poepleVo) {
		if (poepleBo != null && poepleVo != null) {
			BeanUtils.copyProperties(poepleBo, poepleVo);
		}
	}

	private void peopleBo2Vo(List<RetiredPeopleBo> boList, List<RetiredPeopleVo> voList) {
		for (RetiredPeopleBo peopleBo : boList) {
			RetiredPeopleVo peopleVo = new RetiredPeopleVo();
			peopleBo2Vo(peopleBo, peopleVo);
			voList.add(peopleVo);
		}
	}

	private Map<String, Object> isUpdate(String jsonParams, Object resultBo)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Map<String, Object> params = new HashMap<String, Object>();
		Iterator<Map.Entry<String, Object>> iterator = JSONObject.fromObject(jsonParams).entrySet().iterator();
		JSONObject resultObject = JSONObject.fromObject(resultBo);

		while (iterator.hasNext()) {
			Entry<String, Object> entry = iterator.next();
			String key = entry.getKey();

			if ("_id".equals(key) || "createTime".equals(key) || "createuid".equals(key)) {
				continue;
			}

			if (entry.getValue() instanceof java.lang.String) {
				String value = (String) entry.getValue();
				if (value.equals(resultObject.get(key))) {
					continue;
				}
				params.put(key, value);
			}
			if (entry.getValue() instanceof net.sf.json.JSONArray) {
				net.sf.json.JSONArray value = (net.sf.json.JSONArray) entry.getValue();
				net.sf.json.JSONArray oldValue = (net.sf.json.JSONArray) resultObject.get(key);
				for (Object object : value) {
					if (value.size() != oldValue.size() || !oldValue.contains(object)) {
						params.put(key, value);
						continue;
					}
				}
			}
			if (entry.getValue() instanceof java.lang.Integer) {
				java.lang.Integer value = (Integer) entry.getValue();
				Integer oldValue = (Integer) resultObject.get(key);
				// 使用Integer判断两个值时使用了地址值比较,需要转换为int
				int x = value;
				int y = oldValue;
				if (x == y) {
					continue;
				}
				params.put(key, value);
			}
		}

		return params;
	}

	@RequestMapping("/test")
	public String test(String str) {
		return restHomeService.test(str) + "===========" + logger.getName();
	}

}
