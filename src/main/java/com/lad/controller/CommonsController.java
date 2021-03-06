package com.lad.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.lad.bo.OptionBo;
import com.lad.bo.UserBo;
import com.lad.service.IMarriageService;
import com.lad.service.IOldFriendService;
import com.lad.service.IShowService;
import com.lad.service.SpouseService;
import com.lad.service.TravelersService;
import com.lad.util.CommonUtil;
import com.lad.util.ERRORCODE;
import com.lad.util.MyException;
import com.lad.vo.CrcularVo;
import com.lad.vo.OptionVo;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.sf.json.JSONObject;

/**
 * 通过改接口获取一些比较通用的数据
 *
 */
@RestController
@RequestMapping("common")
@SuppressWarnings("all")
public class CommonsController extends ExtraController {

	private Logger logger = LogManager.getLogger();

	@Autowired
	public IMarriageService marriageService;

	@Autowired
	public TravelersService travelersService;

	@Autowired
	private SpouseService spouseService;

	@Autowired
	private IOldFriendService oldFriendService;

	@Autowired
	private IShowService showService;
	

	@GetMapping("crcular-pull")
	public String getCrcular(HttpServletRequest request) {
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		
		List<CrcularVo> pull = pull(userBo.getId());
		Map<String,Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("result", pull);
		return JSON.toJSONString(map);
	}

	@ApiOperation("养老模块-特色服务选项卡")
	@GetMapping("get-ylKnightService")
	public String getYlKnightService() {
		String field = "ylKnightService";
		List<OptionBo> ylOptions = marriageService.getYlOptions(field);
		List<OptionVo> resultList = new ArrayList<>();
		if (ylOptions != null && ylOptions.size() > 0) {
			getVoList(ylOptions, resultList);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("result", resultList);
		return JSON.toJSONString(map);
	}

	@ApiOperation("养老模块-价格区间选项卡")
	@GetMapping("get-ylPrice")
	public String getYlPrice() {
		String field = "ylPrice";
		List<OptionBo> ylOptions = marriageService.getYlOptions(field);
		List<OptionVo> resultList = new ArrayList<>();
		if (ylOptions != null && ylOptions.size() > 0) {
			getVoList(ylOptions, resultList);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("result", resultList);
		return JSON.toJSONString(map);
	}

	@ApiOperation("养老模块-机构性质选项卡")
	@GetMapping("get-ylProperty")
	public String getYlProperty() {
		String field = "ylProperty";
		List<OptionBo> ylOptions = marriageService.getYlOptions(field);
		List<OptionVo> resultList = new ArrayList<>();
		if (ylOptions != null && ylOptions.size() > 0) {
			getVoList(ylOptions, resultList);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("result", resultList);
		return JSON.toJSONString(map);
	}

	@ApiOperation("养老模块-总床位数选项卡")
	@GetMapping("get-ylSeat")
	public String getYlSeat() {
		String field = "ylSeat";
		List<OptionBo> ylOptions = marriageService.getYlOptions(field);
		List<OptionVo> resultList = new ArrayList<>();
		if (ylOptions != null && ylOptions.size() > 0) {
			getVoList(ylOptions, resultList);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("result", resultList);
		return JSON.toJSONString(map);
	}

	@ApiOperation("养老模块-收住对象选项卡")
	@GetMapping("get-ylServiceLevel")
	public String getYlServiceLevel() {
		String field = "ylServiceLevel";
		List<OptionBo> ylOptions = marriageService.getYlOptions(field);
		List<OptionVo> resultList = new ArrayList<>();
		if (ylOptions != null && ylOptions.size() > 0) {
			getVoList(ylOptions, resultList);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("result", resultList);
		return JSON.toJSONString(map);
	}

	@ApiOperation("养老模块-机构类型选项卡")
	@GetMapping("get-ylType")
	public String getYlType() {
		String field = "ylType";
		List<OptionBo> ylOptions = marriageService.getYlOptions(field);
		List<OptionVo> resultList = new ArrayList<>();
		if (ylOptions != null && ylOptions.size() > 0) {
			getVoList(ylOptions, resultList);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("result", resultList);
		return JSON.toJSONString(map);
	}

	@GetMapping("getYlOptions")
	public String getYlOptions() {
		List<OptionBo> ylOptions = marriageService.getYlOptions();
		List<OptionVo> resultList = new ArrayList<>();
		if (ylOptions != null && ylOptions.size() > 0) {
			getVoList(ylOptions, resultList);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("result", resultList);
		return JSON.toJSONString(map);
	}

	@GetMapping("getHobOptions")
	public String getHobbysOptions() {
		List<OptionBo> supOptions = marriageService.getHobbysSupOptions();
		Map<String, Set<String>> map = new HashMap<>();
		for (OptionBo supOption : supOptions) {
			List<OptionBo> sonOptions = marriageService.getHobbysSonOptions(supOption.getId());
			Set<String> sonOptionSet = new LinkedHashSet<>();
			for (OptionBo sonOption : sonOptions) {
				sonOptionSet.add(sonOption.getValue());
			}
			map.put(supOption.getValue(), sonOptionSet);
		}
		Map result = new HashMap<>();
		result.put("ret", 0);
		result.put("result", map);
		return JSON.toJSONString(result);
	}

	/**
	 * 获取当前用户发布消息的条数
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@GetMapping("getNums")
	public String getPublishNum(HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		Map map = new HashMap<>();
		map.put("ret", 0);
		int marriageBoyNum = marriageService.findPublishNum(userBo.getId());
		map.put("marriageBoyNum", marriageBoyNum);
		int marriageGirlNum = marriageService.findPublishGirlNum(userBo.getId());
		map.put("marriageGirlNum", marriageGirlNum);
		int travelersNum = travelersService.findPublishNum(userBo.getId());
		map.put("travelersNum", travelersNum);
		int spouseNum = spouseService.findPublishNum(userBo.getId());
		map.put("spouseNum", spouseNum);
		int oldFriendNum = oldFriendService.findPublishNum(userBo.getId());
		map.put("oldFriendNum", oldFriendNum);
		int showZhaoNum = showService.findPublishZhaoNum(userBo.getId());
		map.put("showZhaoNum", showZhaoNum);
		int showJieNum = showService.findPublishJieNum(userBo.getId());
		map.put("showJieNum", showJieNum);
		int restHomeNum = restHomeService.findPublishHomeNum(userBo.getId());
		map.put("restHomeNum", restHomeNum);
		int retiredPeopleNum = restHomeService.findPublishPeopleNum(userBo.getId());
		map.put("retiredPeopleNum", retiredPeopleNum);

		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("查询选项")
	@GetMapping("/options-all-search")
	public String getAllOptions(HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		List<OptionBo> options = marriageService.getOptions();

		Map<String, List<OptionBo>> map = new HashMap<>();
		List<OptionBo> salarys = new ArrayList<>();
		List<OptionBo> job = new ArrayList<>();
		List<OptionBo> hobbys = new ArrayList<>();
		for (OptionBo optionBo : options) {
			if ("salary".equals(optionBo.getField())) {
				salarys.add(optionBo);
			}
			if ("job".equals(optionBo.getField())) {
				job.add(optionBo);
			}
			if ("hobbys".equals(optionBo.getField())) {
				hobbys.add(optionBo);
			}
		}
		map.put("salary", salarys);
		map.put("job", job);
		map.put("hobbys", hobbys);
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("查询选项")
	@PostMapping("/options-search")
	public String getOptions(
			@RequestBody @ApiParam(name = "optionVo", value = "封装前端请求参数的实体", required = true) OptionVo ov,
			HttpServletRequest request, HttpServletResponse response) {
		List<OptionBo> options = marriageService.getOptions(ov);
		if (options == null) {
			return "无对应选项";
		}
		String jsonString = JSON.toJSONString(options);
		return jsonString;
	}

	@GetMapping("job-options")
	public String getJobOptions() {
		List<OptionBo> jobOptions = marriageService.getJobOptions();
		return JSON.toJSONString(jobOptions);
	}

	@GetMapping("salary-options")
	public String getSalaryOptions() {
		List<OptionBo> salOptions = marriageService.getSalaryOptions();
		return JSON.toJSONString(salOptions);
	}

	private void getVoList(List<OptionBo> boList, List<OptionVo> voList) {

		try {
			for (OptionBo optionBo : boList) {
				OptionVo optionVo = new OptionVo();
				BeanUtils.copyProperties(optionBo, optionVo);
				voList.add(optionVo);
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
}
