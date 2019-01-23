package com.lad.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.api.RLock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.lad.bo.CareBo;
import com.lad.bo.CircleBo;
import com.lad.bo.DynamicBo;
import com.lad.bo.NoteBo;
import com.lad.bo.OptionBo;
import com.lad.bo.RestHomeBo;
import com.lad.bo.RetiredPeopleBo;
import com.lad.bo.UserBo;
import com.lad.constants.GeneralContants;
import com.lad.constants.UserCenterConstants;
import com.lad.service.IDynamicService;
import com.lad.service.IFriendsService;
import com.lad.service.IMarriageService;
import com.lad.service.IRestHomeService;
import com.lad.service.IUserService;
import com.lad.util.CommonUtil;
import com.lad.util.Constant;
import com.lad.util.ERRORCODE;
import com.lad.util.MyException;
import com.lad.vo.BaseVo;
import com.lad.vo.RestHomeVo;
import com.lad.vo.RetiredPeopleVo;
import com.lad.vo.UserBaseVo;
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
public class RestHomeController extends ExtraController {

	private static final Logger logger = LogManager.getLogger(RestHomeController.class);

	@Autowired
	private IMarriageService marriageService;

	@Autowired
	private AsyncController asyncController;

    @ApiOperation("用户关注/取消关注老人")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "peopleId", value = "被关注/取消的养老者id", required = true, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "isCare", value = "关注/取消关注", required = true, dataType = "bool", paramType = "query")
    })
    @PostMapping("/care-people")
    public String carePeople(String peopleId,boolean isCare,HttpServletRequest request,HttpServletResponse response){
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        RetiredPeopleBo people = restHomeService.findPeopleById(peopleId);
        if (null == people) {
            return CommonUtil.toErrorResult(ERRORCODE.PEOPLE_IS_NULL.getIndex(), ERRORCODE.PEOPLE_IS_NULL.getReason());
        }

        Map<String, Object> map = new HashMap<>();
        if(isCare){
            map.put("ret", addCare(userBo, people, CareBo.CARE_CARE));
        }else{
            List<String> peoples = new ArrayList<>();
            peoples.add(peopleId);
            WriteResult result = careService.delCareListByUidAndTyeAndOids(userBo.getId(), CareBo.CARE_PEOPLE, peoples);
            map.put("ret", 0);
        }
        return JSON.toJSONString(map);
    }

    @ApiOperation("用户关注/取消关注养老院")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "homeId", value = "被关注/取消的养老院id", required = true, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "isCare", value = "关注/取消关注", required = true, dataType = "bool", paramType = "query")
    })
    @PostMapping("/care-home")
	public String careHome(String homeId,boolean isCare,HttpServletRequest request,HttpServletResponse response){
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        RestHomeBo home = restHomeService.findHomeById(homeId);
        if (null == home) {
            return CommonUtil.toErrorResult(ERRORCODE.HOME_IS_NULL.getIndex(), ERRORCODE.HOME_IS_NULL.getReason());
        }

        Map<String, Object> map = new HashMap<>();
        if(isCare){
            map.put("ret", addCare(userBo, home, CareBo.CARE_CARE));
        }else{
            List<String> homes = new ArrayList<>();
            homes.add(homeId);
            WriteResult result = careService.delCareListByUidAndTyeAndOids(userBo.getId(), CareBo.CARE_RESTHOME, homes);
            map.put("ret", 0);
        }
        return JSON.toJSONString(map);
    }

	@ApiOperation("用户关注的老人列表,需要当前登录用户是养老院的创建者")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "page", value = "当前页", required = true, dataType = "integer", paramType = "query"),
			@ApiImplicitParam(name = "limit", value = "每页要显示的条数", required = true, dataType = "integer", paramType = "query")
	})
	@PostMapping("/care-list-home")
	public String homeCareList(int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		logger.info("@PostMapping(\"/care-list-home\")=====page:{},limit:{}", page, limit);
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		List<CareBo> careList = careService.findCareListByUidAndTye(userBo.getId(), CareBo.CARE_PEOPLE, page, limit);
		List<RetiredPeopleVo> res = new ArrayList<>();
		for (CareBo careBo : careList) {
			String peopleId = careBo.getOid();
			RetiredPeopleBo peopleBo = restHomeService.findPeopleById(peopleId);
			RetiredPeopleVo peopleVo = new RetiredPeopleVo();
			peopleBo2Vo(peopleBo, peopleVo);
			res.add(peopleVo);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("res", res);
		return JSON.toJSONString(map);
	}

	@ApiOperation("用户关注的养老院列表,需要当前登录用户是people的创建者")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "page", value = "当前页", required = true, dataType = "integer", paramType = "query"),
			@ApiImplicitParam(name = "limit", value = "每页要显示的条数", required = true, dataType = "integer", paramType = "query") })
	@PostMapping("/care-list-people")
	public String poepleCareList(int page, int limit, HttpServletRequest request,HttpServletResponse response) {
		logger.info("@PostMapping(\"/care-list-people\")=====page:{},limit:{}", page, limit);
		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		// 用户校验
		List<CareBo> careList = careService.findCareListByUidAndTye(userBo.getId(), CareBo.CARE_RESTHOME, page, limit);
		List<RestHomeVo> res = new ArrayList<>();
		for (CareBo careBo : careList) {
			String homeId = careBo.getOid();
			RestHomeBo homeBo = restHomeService.findHomeById(homeId);
			RestHomeVo homeVo = new RestHomeVo();
			homeBo2Vo(homeBo, homeVo);
			res.add(homeVo);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("res", res);
		return JSON.toJSONString(map);
	}

	@ApiOperation("转发养老院到我的动态")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "homeId", value = "被转发的养老院id", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "view", value = "转发说明信息", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "landmark", value = "转发时的地标", dataType = "string", paramType = "query") })
	@PostMapping("/forward-dynamic")
	public String forwardDynamic(String homeId, String view, String landmark, HttpServletRequest request,
			HttpServletResponse response) {

		UserBo userBo;
		try {
			userBo = checkSession(request, userService);
		} catch (MyException e) {
			return e.getMessage();
		}
		logger.info("@PostMapping(\"/forward-dynamic\")=====homeId:{},view:{},landmark:{},user:{}({})", homeId, view,
				landmark, userBo.getUserName(), userBo.getId());

		RestHomeBo home = restHomeService.findHomeById(homeId);
		if (null == home) {
			return CommonUtil.toErrorResult(ERRORCODE.HOME_IS_NULL.getIndex(), ERRORCODE.HOME_IS_NULL.getReason());
		}
		DynamicBo dynamicBo = new DynamicBo();
		dynamicBo.setTitle(home.getName());
		dynamicBo.setView(view);
		dynamicBo.setForward(GeneralContants.YES);
		dynamicBo.setSourceId(homeId);
		dynamicBo.setCreateuid(userBo.getId());
		dynamicBo.setOwner(home.getCreateuid());
		dynamicBo.setLandmark(landmark);
		dynamicBo.setType(UserCenterConstants.FORWARD_FROM_DISCOVERY_RESTHOME);
		if (home.getImages() != null && home.getImages().size() > 0) {
//			dynamicBo.setPicType("pic");
			dynamicBo.setPhotos(home.getImages());
		}
		dynamicBo.setSourceName(home.getName());

		List<String> friends = CommonUtil.deleteBack(dynamicService, friendsService, userBo);
		dynamicBo.setUnReadFrend(new LinkedHashSet<>(friends));
		dynamicService.addDynamic(dynamicBo);
		updateCount(homeId, Constant.SHARE_NUM, 1);
		updateDynamicNums(userBo.getId(), 1, dynamicService, redisServer);
		updateHomeHot(home.getId(), 1, Constant.HOME_SHARE);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("dynamicid", dynamicBo.getId());
		return JSONObject.fromObject(map).toString();
	}

	@ApiOperation("转发指定的圈子")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "circleid", value = "转发圈子id", required = true, paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "homeId", value = "养老院id", required = true, paramType = "query", dataType = "string") })
	@RequestMapping(value = "/forward-circle", method = { RequestMethod.GET, RequestMethod.POST })
	public String forwardCircle(String circleid, String homeId, HttpServletRequest request,
			HttpServletResponse response) {
		return forwardCircle(circleid, homeId, null, request, response);
	}



	@ApiOperation("搜索住院者")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "price", value = "住院者心理价位", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "显示条数", required = true, paramType = "query", dataType = "int") })
	@PostMapping("/people-search")
	public String homeSearch(int price, int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/people-search\")=====price:{},page:{},limit:{}", price, page, limit);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}

			if (price < 0 || price > Integer.MAX_VALUE) {
				map.put("ret", -1);
				map.put("message", "错误的价格区间");
				return JSON.toJSONString(map);
			}

			List<OptionBo> ylOptions = marriageService.getYlOptions("ylPrice");
			String peoplePrice = null;
			if (price > 10000) {
				peoplePrice = "10000以上";

			} else {
				for (OptionBo optionBo : ylOptions) {
					String value = optionBo.getValue();
					if ("10000以上".equals(value)) {
						continue;
					}
					int[] numInStr = CommonUtil.numInStr(value);
					if (numInStr.length != 2) {
						throw new Exception("@PostMapping(\"/people-search\"):价格选项卡错误( option.value =" + value + ")");
					}
					if (price >= numInStr[0] && price <= numInStr[1]) {
						peoplePrice = value;
						continue;

					}

				}
			}

			List<RestHomeBo> homeList = restHomeService.findHomeListByUid(userBo.getId());

			List<Map<String, Object>> conditionList = new ArrayList<>();
			for (RestHomeBo restHomeBo : homeList) {
				if (restHomeBo != null) {
					Map<String, Object> condition = new HashMap<>();
					condition.put("acceptOtherArea", restHomeBo.isAcceptOtherArea());
					condition.put("area", CommonUtil.getCity(restHomeBo.getArea()));
					conditionList.add(condition);
				}
			}

			List<RetiredPeopleBo> retiredPeople = restHomeService.findPeopleListByPrice(userBo.getId(), conditionList,
					peoplePrice, page, limit);
			List<RetiredPeopleVo> resultList = new ArrayList<>();
			if (retiredPeople != null && retiredPeople.size() > 0) {
				peopleBo2Vo(retiredPeople, resultList);

			}
			map.put("ret", 0);
			map.put("result", resultList);
		} catch (Exception e) {
			logger.error("@PostMapping(\"/people-search\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误:" + e.toString());
		}

		return JSON.toJSONString(map);
	}

	@ApiOperation("搜索养老院")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "keyword", value = "养老院地区关键词", required = true, paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "显示条数", required = true, paramType = "query", dataType = "int") })
	@PostMapping("/home-search")
	public String homeSearch(String keyword, int page, int limit, HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/home-search\")=====keyword:{},page:{},limit:{}", keyword, page, limit);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}
			if (StringUtils.isEmpty(keyword)) {
				map.put("ret", -1);
				map.put("message", "关键词不能为空");
			}
			keyword = ".*" + keyword + ".*";

			List<RestHomeBo> restHome = restHomeService.findHomeListByKeyword(userBo.getId(), keyword, page, limit);
			List<RestHomeVo> resultList = new ArrayList<>();
			if (restHome != null && restHome.size() > 0) {
				homeBo2Vo(restHome, resultList);
			}

			map.put("ret", 0);
			map.put("result", resultList);
		} catch (Exception e) {
			logger.error("@PostMapping(\"/home-search\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误:" + e.toString());
		}

		return JSON.toJSONString(map);
	}

	@ApiOperation("养老院列表")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "显示条数", required = true, paramType = "query", dataType = "int") })
	@PostMapping("/home-list")
	public String homeList(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/home-list\")=====page:{},limit:{}", page, limit);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}

			List<RestHomeBo> restHome = restHomeService.findHomeList(userBo.getId(), page, limit);
			List<RestHomeVo> resultList = new ArrayList<>();
			if (restHome != null && restHome.size() > 0) {
				homeBo2Vo(restHome, resultList);
			}

			map.put("ret", 0);
			map.put("result", resultList);

		} catch (Exception e) {
			logger.error("@PostMapping(\"/home-list\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误:" + e.toString());
		}

		return JSON.toJSONString(map);
	}

	@ApiOperation("推荐养老院(泛推)")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "显示条数", required = true, paramType = "query", dataType = "int") })
	@PostMapping("/recommend-home-all")
	public String recommendHome(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/recommend-home-all\")=====page:{},limit:{}", page, limit);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}

			List<RetiredPeopleBo> peopleList = restHomeService.findPeopleByUserid(userBo.getId());
			List<Map<String, String>> areaList = new ArrayList<>();
			for (RetiredPeopleBo retiredPeopleBo : peopleList) {
				if (retiredPeopleBo != null) {
					Map<String, String> areaMap = new HashMap<>();
					areaMap.put("wannaArea", CommonUtil.getCity(retiredPeopleBo.getHomeArea()));
					areaMap.put("homeArea", CommonUtil.getCity(retiredPeopleBo.getHomeArea()));
				}
			}

			List<RestHomeBo> recommendBo = restHomeService.findRecommendHome(userBo.getId(), areaList, page, limit);
			List<RestHomeVo> resultList = new ArrayList<>();
			if (recommendBo != null && recommendBo.size() > 0) {
				homeBo2Vo(recommendBo, resultList);
			}

			map.put("ret", 0);
			map.put("result", resultList);

		} catch (Exception e) {
			logger.error("@PostMapping(\"/recommend-home-all\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误:" + e.toString());
		}

		return JSON.toJSONString(map);
	}

	@ApiOperation("推荐养老院(专推)")
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

			RetiredPeopleBo people = restHomeService.findPeopleById(peopleId);
			if (people == null) {
				map.put("ret", -1);
				map.put("message", "老人信息不存在或已删除");
				return JSON.toJSONString(map);
			}

			String wannaArea = CommonUtil.getCity(people.getWannaArea());
			String homeArea = CommonUtil.getCity(people.getHomeArea());

			List<RestHomeBo> recommendBo = restHomeService.findRecommendHome(userBo.getId(), homeArea, wannaArea, page,
					limit);
			List<RestHomeVo> resultList = new ArrayList<>();
			if (recommendBo != null && recommendBo.size() > 0) {
				homeBo2Vo(recommendBo, resultList);
			}

			map.put("ret", 0);
			map.put("result", resultList);

		} catch (Exception e) {
			logger.error("@PostMapping(\"/recommend-home\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误:" + e.toString());
		}

		return JSON.toJSONString(map);
	}

	@ApiOperation("推荐住院人(泛推)")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "limit", value = "显示条数", required = true, paramType = "query", dataType = "int") })
	@PostMapping("/recommend-people-all")
	public String recommendPeople(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/recommend-people-all\")=====page:{},limit:{}", page, limit);
		try {
			UserBo userBo = getUserLogin(request);
			if (userBo == null) {
				return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
						ERRORCODE.ACCOUNT_OFF_LINE.getReason());
			}

			List<RestHomeBo> homeList = restHomeService.findHomeListByUid(userBo.getId());
			List<Map<String, Object>> conditionList = new ArrayList<>();
			for (RestHomeBo restHomeBo : homeList) {
				if (restHomeBo != null) {
					Map<String, Object> condition = new HashMap<>();
					condition.put("acceptOtherArea", restHomeBo.isAcceptOtherArea());
					condition.put("area", CommonUtil.getCity(restHomeBo.getArea()));
					conditionList.add(condition);
				}
			}

			List<RetiredPeopleBo> recommendBo = restHomeService.findRecommendPeople(userBo.getId(), conditionList, page,
					limit);
			List<RetiredPeopleVo> resultList = new ArrayList<>();
			if (recommendBo != null && recommendBo.size() > 0) {
				peopleBo2Vo(recommendBo, resultList);

			}
			map.put("ret", 0);
			map.put("result", resultList);

		} catch (Exception e) {
			logger.error("@PostMapping(\"/recommend-people-all\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误:" + e.toString());
		}

		return JSON.toJSONString(map);
	}

	@ApiOperation("推荐住院人(专推)")
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

			RestHomeBo home = restHomeService.findHomeById(homeId);
			if (home == null) {
				map.put("ret", -1);
				map.put("message", "养老院信息不存在或已删除");
				return JSON.toJSONString(map);
			}
			boolean acceptOtherArea = home.isAcceptOtherArea();
			String area = CommonUtil.getCity(home.getArea());

			List<RetiredPeopleBo> recommendBo = restHomeService.findRecommendPeople(userBo.getId(), area,
					acceptOtherArea, page, limit);
			List<RetiredPeopleVo> resultList = new ArrayList<>();
			if (recommendBo != null && recommendBo.size() > 0) {
				peopleBo2Vo(recommendBo, resultList);

			}
			map.put("ret", 0);
			map.put("result", resultList);

		} catch (Exception e) {
			logger.error("@PostMapping(\"/recommend-people\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误:" + e.toString());
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
			map.put("message", "服务器内部错误:" + e.toString());
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
			map.put("message", "服务器内部错误:" + e.toString());
		}

		return JSON.toJSONString(map);
	}

	@ApiOperation("修改养老信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "homeJson", value = "养老院json数据", required = true, paramType = "query", dataType = "string") })
	@PostMapping("/updateHome")
	public String updateHome(String homeJson, HttpServletRequest request, HttpServletResponse response) {
		logger.info("@PostMapping(\"/updateHome\")=====homeJson:{}", homeJson);

		Map<String, Object> map = new HashMap<>();
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

			homeJson = jsonHandler(homeJson);

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
			restHomeService.updateHomeById(homeBo.getId(), params);
			map.put("ret", 0);
			map.put("homeId", homeBo.getId());
			map.put("message", "修改成功");
			/*
			 * WriteResult result = restHomeService.updateHomeById(homeBo.getId(), params);
			 * if (result.isUpdateOfExisting()) { map.put("ret", 0); map.put("message",
			 * "修改成功"); } else { map.put("ret", -1); map.put("message", "修改失败"); }
			 */
		} catch (com.alibaba.fastjson.JSONException e) {
			logger.error("@PostMapping(\"/updateHome\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "JSON格式错误:" + e.toString());
		} catch (JSONException e) {
			logger.error("@PostMapping(\"/updateHome\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "JSON格式错误:" + e.toString());
		} catch (Exception e) {
			logger.error("@PostMapping(\"/updateHome\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误:" + e.toString());
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
				return JSON.toJSONString(map);
			}

			RetiredPeopleBo poepleBo = JSON.parseObject(peopleJson, RetiredPeopleBo.class);
			if (poepleBo.getId() == null) {
				map.put("ret", -1);
				map.put("message", "缺少id");
				return JSON.toJSONString(map);
			}
			RetiredPeopleBo resultBo = restHomeService.findPeopleById(poepleBo.getId());
			if (resultBo == null) {
				map.put("ret", -1);
				map.put("message", "数据不存在或已删除");
				return JSON.toJSONString(map);
			}

			Map<String, Object> params = isUpdate(peopleJson, resultBo);

			restHomeService.updatePeopleById(poepleBo.getId(), params);
			map.put("ret", 0);
			map.put("peopleId", poepleBo.getId());
			map.put("message", "修改成功");
			/*
			 * WriteResult result = restHomeService.updatePeopleById(poepleBo.getId(),
			 * params); if (result.isUpdateOfExisting()) { map.put("ret", 0);
			 * map.put("message", "修改成功"); } else { map.put("ret", -1); map.put("message",
			 * "修改失败"); }
			 */

		} catch (com.alibaba.fastjson.JSONException e) {
			logger.error("@PostMapping(\"/updatePoeple\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "JSON格式错误:" + e.toString());
		} catch (JSONException e) {
			logger.error("@PostMapping(\"/updatePoeple\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "JSON格式错误:" + e.toString());
		} catch (Exception e) {
			logger.error("@PostMapping(\"/updatePoeple\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误:" + e.toString());
		}
		return JSON.toJSONString(map);
	}

	@ApiOperation("住院者详情")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "peopleId", value = "住院者ID", required = true, paramType = "query", dataType = "string") })
	@PostMapping("/peopleInfo")
	public String peopleInfo(String peopleId, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		logger.info("@PostMapping(\"/peopleInfo\")=====peopleId:{}", peopleId);
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

			if (peopleBo != null) {
				RetiredPeopleVo peopleVo = new RetiredPeopleVo();
				UserBo createUser = userService.findUserById(peopleBo.getCreateuid());
				peopleBo2Vo(peopleBo, peopleVo);

				map.put("ret", 0);
				map.put("result", peopleVo);
				UserBaseVo baseVo = new UserBaseVo();
				BeanUtils.copyProperties(createUser, baseVo);
				map.put("createuser", baseVo);
				map.put("isCare", careService.findCareByUidAndOid(userBo.getId(), peopleId, CareBo.CARE_PEOPLE) != null);
			} else {
				map.put("ret", -1);
				map.put("message", "数据不存在或已删除");
			}

		} catch (Exception e) {
			logger.error("@PostMapping(\"/peopleInfo\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误:" + e.toString());
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
			if (homeBo != null) {
				UserBo createUser = userService.findUserById(homeBo.getCreateuid());
				RestHomeVo homeVo = new RestHomeVo();
				homeBo2Vo(homeBo, homeVo);

				map.put("ret", 0);
				map.put("result", homeVo);
				UserBaseVo baseVo = new UserBaseVo();
				BeanUtils.copyProperties(createUser, baseVo);
				map.put("createuser", baseVo);
				map.put("isCare", careService.findCareByUidAndOid(userBo.getId(), homeId, CareBo.CARE_RESTHOME) != null);
			} else {
				map.put("ret", -1);
				map.put("message", "数据不存在或已删除");
			}

		} catch (Exception e) {
			logger.error("@PostMapping(\"/homeInfo\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误:" + e.toString());
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
				restHomeService.deletePeopleById(peopleId);
				map.put("ret", 0);
				map.put("message", "删除成功");

				/*
				 * WriteResult result = restHomeService.deletePeopleById(peopleId); if
				 * (result.isUpdateOfExisting()) { map.put("ret", 0); map.put("message",
				 * "删除成功"); } else { map.put("ret", -1); map.put("message", "删除失败"); }
				 */
			} else {
				map.put("ret", 0);
				map.put("message", "删除成功");
			}

		} catch (Exception e) {
			logger.error("@PostMapping(\"/deletePeople\")====={}", e);
			map.put("ret", -1);
			map.put("description", e.toString());
			map.put("message", "服务器内部错误:" + e.toString());
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
				restHomeService.deleteHomeById(homeId);
				map.put("ret", 0);
				map.put("message", "删除成功");
				/*
				 * WriteResult result = restHomeService.deleteHomeById(homeId); if
				 * (result.isUpdateOfExisting()) { map.put("ret", 0); map.put("message",
				 * "删除成功"); } else { map.put("ret", -1); map.put("message", "删除失败"); }
				 */
			} else {
				map.put("ret", 0);
				map.put("message", "删除成功");
			}
		} catch (Exception e) {
			logger.error("@PostMapping(\"/deleteHome\")====={}", e);
			map.put("ret", -1);
			map.put("message", "服务器内部错误:" + e.toString());
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

			resthomeJson = jsonHandler(resthomeJson);

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
			map.put("message", "JSON格式错误:" + e.toString());
		} catch (JSONException e) {
			logger.error("@PostMapping(\"/inserthome\")====={}", e);
			map.put("ret", -1);
			map.put("message", "JSON格式错误:" + e.toString());
		} catch (Exception e) {
			logger.error("@PostMapping(\"/inserthome\")====={}", e);
			map.put("ret", -1);
			map.put("message", "服务器内部错误:" + e.toString());
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
					map.put("peopleId", poepleBo.getId());
				} else {
					map.put("ret", -1);
					map.put("message", "请查看用户协议");
				}
			}
		} catch (com.alibaba.fastjson.JSONException e) {
			logger.error("@PostMapping(\"/insertpoeple\")====={}", e);
			map.put("ret", -1);
			map.put("message", "JSON格式错误:" + e.toString());
		} catch (JSONException e) {
			logger.error("@PostMapping(\"/insertpoeple\")====={}", e);
			map.put("ret", -1);
			map.put("message", "JSON格式错误:" + e.toString());
		} catch (Exception e) {
			logger.error("@PostMapping(\"/insertpoeple\")====={}", e);
			map.put("ret", -1);
			map.put("message", "服务器内部错误:" + e.toString());
		}
		return JSON.toJSONString(map);
	}


	private String jsonHandler(String resthomeJson) {
		// 特殊处理 传入参数将最低价值与最高价值拆分为两个字段
		com.alibaba.fastjson.JSONObject parseObject = JSON.parseObject(resthomeJson);
		int highest = (int) parseObject.get("priceHighest");
		int lowest = (int) parseObject.get("priceLowest");
		String price = lowest + "-" + highest;
		parseObject.remove("priceHighest");
		parseObject.remove("priceLowest");
		parseObject.put("price", price);
		resthomeJson = JSON.toJSONString(parseObject);
		return resthomeJson;
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

	private Map<String, Object> isUpdate(String jsonParams, Object oldValues)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

		Map<String, Object> params = new HashMap<String, Object>();
		Iterator<Map.Entry<String, Object>> iterator = JSONObject.fromObject(jsonParams).entrySet().iterator();
		JSONObject oldObject = JSONObject.fromObject(oldValues);

		while (iterator.hasNext()) {
			Entry<String, Object> entry = iterator.next();
			String key = entry.getKey();

			if ("_id".equals(key) || "createTime".equals(key) || "createuid".equals(key)) {
				continue;
			}
			if (entry.getValue() instanceof java.lang.Boolean) {
				boolean value = (boolean) entry.getValue();
				boolean oldValue = (boolean) oldObject.get(key);
				if (value == oldValue) {
					continue;
				}
				params.put(key, value);
			}
			if (entry.getValue() instanceof java.lang.String) {
				String value = (String) entry.getValue();
				if (value.equals(oldObject.get(key))) {
					continue;
				}
				params.put(key, value);
			}
			if (entry.getValue() instanceof net.sf.json.JSONArray) {
				net.sf.json.JSONArray value = (net.sf.json.JSONArray) entry.getValue();
				net.sf.json.JSONArray oldValue = (net.sf.json.JSONArray) oldObject.get(key);
				for (Object object : value) {
					if (value.size() != oldValue.size() || !oldValue.contains(object)) {
						params.put(key, value);
						continue;
					}
				}
			}
			if (entry.getValue() instanceof java.lang.Integer) {
				java.lang.Integer value = (Integer) entry.getValue();
				Integer oldValue = (Integer) oldObject.get(key);
				// 使用Integer判断两个值时使用了地址值比较,需要手动转换为int
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
	
	/**
	 * 作为一个可扩展的私有handler
	 * 
	 * @param circleid
	 * @param homeId
	 * @param landmark
	 * @param request
	 * @param response
	 * @return
	 */
	private String forwardCircle(String circleid, String homeId, String landmark, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("@RequestMapping(value = \"/forward-dynamic\")=====user:{}({}),circleid:{},homeId:{}",
				userBo.getUserName(), userBo.getId(), circleid, homeId);
		RestHomeBo home = restHomeService.findHomeById(homeId);
		if (null == home) {
			return CommonUtil.toErrorResult(ERRORCODE.HOME_IS_NULL.getIndex(), ERRORCODE.HOME_IS_NULL.getReason());
		}
		CircleBo circleBo = circleService.selectById(circleid);
		if (circleBo == null) {
			return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_IS_NULL.getIndex(), ERRORCODE.CIRCLE_IS_NULL.getReason());
		}

		NoteBo noteBo = new NoteBo();
		noteBo.setSourceid(homeId);
		noteBo.setNoteType(NoteBo.REST_FORWARD);
		noteBo.setForward(1);
		noteBo.setCreateuid(userBo.getId());
		noteBo.setCircleId(circleid);
		noteBo.setCreateDate(CommonUtil.getCurrentDate(new Date()));
		if (landmark != null) {
			noteBo.setLandmark(landmark);
		}
//		String[] atUser = atUserids.split(",");
//		noteBo.setAtUsers(new LinkedList<>(Arrays.asList(atUser)));

		NoteBo insert = noteService.insert(noteBo);
		// 更新圈子成员未读帖子列表 重写了updateCircieNoteUnReadNum,添加了noteId字段
		asyncController.updateCircieNoteUnReadNum(userBo.getId(), circleid, insert.getId());
		updateCount(homeId, Constant.SHARE_NUM, 1);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("noteId", insert.getId());
		return JSONObject.fromObject(map).toString();
	}

	private void updateCount(String shareId, int type, int num) {
		RLock lock = redisServer.getRLock(shareId.concat(String.valueOf(type)));
		try {
			lock.lock(2, TimeUnit.SECONDS);
			switch (type) {

			case Constant.SHARE_NUM:// 分享
				restHomeService.updateTransCount(shareId, num);
				break;
			default:
				break;
			}
		} finally {
			lock.unlock();
		}
	}

	private void updateHomeHot(String homeId, int num, int type) {
		RLock lock = redisServer.getRLock(Constant.CHAT_LOCK);
		try {
			// 3s自动解锁
			lock.lock(3, TimeUnit.SECONDS);
			restHomeService.updateHomeHot(homeId, num, type);
		} finally {
			lock.unlock();
		}
	}
}
