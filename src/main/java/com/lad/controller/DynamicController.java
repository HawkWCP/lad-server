package com.lad.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.lad.bo.CommentBo;
import com.lad.bo.DynamicBackBo;
import com.lad.bo.DynamicBo;
import com.lad.bo.HomepageBo;
import com.lad.bo.UserBo;
import com.lad.bo.UserVisitBo;
import com.lad.constants.UserCenterConstants;
import com.lad.util.CommonUtil;
import com.lad.util.Constant;
import com.lad.util.ERRORCODE;
import com.lad.util.MyException;
import com.lad.vo.DynamicVo;
import com.lad.vo.UserBaseVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 功能描述： Copyright: Copyright (c) 2017 Version: 1.0 Time:2017/9/23
 */
@Api("动态信息相关接口")
@RestController
@RequestMapping("/dynamic")
public class DynamicController extends ExtraController {

    private Logger logger = LogManager.getLogger(DynamicController.class);
    private String pushTitle = "动态通知";


    @ApiOperation("删除动态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dynamicId", value = "要删除的动态id", required = true, paramType = "query", dataType = "string")
    })
    @PostMapping("dynamic-delete")
    public String dynamicDel(String dynamicId, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>();
        UserBo userBo = getUserLogin(request);
        if (userBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                    ERRORCODE.ACCOUNT_OFF_LINE.getReason());
        }
        DynamicBo dynamicBo = dynamicService.findDynamicById(dynamicId);

        if (dynamicBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.DYNAMIC_IS_NULL.getIndex(),
                    ERRORCODE.DYNAMIC_IS_NULL.getReason());
        }

        if (!userBo.getId().equals(dynamicBo.getCreateuid())) {
            return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_MASTER_NULL.getIndex(),
                    ERRORCODE.CIRCLE_MASTER_NULL.getReason());
        }
        dynamicService.deleteDynamic(dynamicId);
        map.put("ret", 0);
        return JSONObject.fromObject(map).toString();
    }

    @ApiOperation("对动态或动态下的评论进行评论")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dynamicId", value = "动态或评论id", required = true, paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "type", value = "评论类型,0表示评论动态,1表示评论评论", paramType = "query", required = true, dataType = "integer"),
            @ApiImplicitParam(name = "content", value = "评论类容", required = true, paramType = "query", dataType = "string")
    })
    @PostMapping("dynamic-comment")
    public String dynamiccomment(String dynamicId, int type, String content, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>();
        UserBo userBo = getUserLogin(request);
        if (userBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                    ERRORCODE.ACCOUNT_OFF_LINE.getReason());
        }
        CommentBo comment = null;
        // 生存着的意义:一开始是说有图片评论,所以放在整个方法的参数区,并且调用的私有方法已经写好了,后来又说没有图片,就设置一个变量闲置防止报错
        LinkedHashSet<String> photos = new LinkedHashSet<String>();
        // type:0 动态;1 评论
        if (type == 0) {
            DynamicBo dynamicBo = dynamicService.findDynamicById(dynamicId);
            if (dynamicBo == null) {
                return CommonUtil.toErrorResult(ERRORCODE.COMMENT_OBJ_NULL.getIndex(),
                        ERRORCODE.COMMENT_OBJ_NULL.getReason());
            }
            comment = comment(userBo, dynamicBo, content, photos);
        } else if (type == 1) {
            CommentBo commentBo = commentService.findById(dynamicId);
            if (commentBo == null) {
                return CommonUtil.toErrorResult(ERRORCODE.COMMENT_OBJ_NULL.getIndex(),
                        ERRORCODE.COMMENT_OBJ_NULL.getReason());
            }
            comment = comment(userBo, commentBo, content, photos);
        }
        map.put("ret", 0);
        map.put("result", comentBo2Vo(comment));
        return JSONObject.fromObject(map).toString();
    }

    @ApiOperation("动态点赞")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dynamicId", value = "动态id", required = true, paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "isThumbsup", value = "点赞:true,取消点赞:false", paramType = "query", required = true)})
    @PostMapping("/thuumbsup")
    public String thumbsup(String dynamicId, boolean isThumbsup, HttpServletRequest request,
                           HttpServletResponse response) {
        logger.info("@PostMapping(\"/thuumbsup\")=====dynamicId:{},isThumbsup:{}", dynamicId, isThumbsup);
        String result = null;
        try {
            UserBo userBo = getUserLogin(request);
            if (userBo == null) {
                return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                        ERRORCODE.ACCOUNT_OFF_LINE.getReason());
            }
            int thumbsup = -1;
            DynamicBo dynamicBo = dynamicService.findDynamicById(dynamicId);
            if (dynamicBo != null) {
                thumbsup = thumbsup(userBo, dynamicBo, isThumbsup);
            }
            if (thumbsup == 0) {
                result = Constant.COM_RESP;
            } else {
                result = Constant.COM_FAIL_RESP;
            }
        } catch (Exception e) {
            result = Constant.COM_FAIL_RESP;
            logger.error("@PostMapping(\"/thuumbsup\")====={}", e);
        }
        return result;
    }

    @ApiOperation("动态详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dynamicId", value = "动态id", required = true, paramType = "query", dataType = "string")})
    @PostMapping(value = "/dynamic-infor")
    public String dynamicInfo(String dynamicId, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>();
        try {
            UserBo userBo = getUserLogin(request);
            if (userBo == null) {
                return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                        ERRORCODE.ACCOUNT_OFF_LINE.getReason());
            }
            logger.info("@PostMapping(value = \"/dynamic-infor\")=====user:{}({}),dynamicId:{}", userBo.getUserName(),
                    userBo.getId(), dynamicId);

            DynamicBo dynamicBo = dynamicService.findDynamicById(dynamicId);
            DynamicVo dynamicVo = new DynamicVo();
            dynamicBo2vo(dynamicBo, dynamicVo, userBo);


            JSONObject parseObject = JSONObject.fromObject(dynamicVo);
//			com.alibaba.fastjson.JSONObject parseObject = JSON.parseObject(JSON.toJSONString(dynamicVo));
            UserBo user = userService.getUser(dynamicBo.getCreateuid());
            parseObject.put("creatorBirthday", user.getBirthDay() == null ? "1970年09月20日" : user.getBirthDay());
            parseObject.put("creatorSex", user.getSex());
            parseObject.put("isSelf", userBo.getId().equals(user.getId()));
            map.put("ret", 0);
            map.put("result", parseObject);
        } catch (Exception e) {
            logger.error("@PostMapping(value = \"/dynamic-infor\") throw exception:{}", e);
            map.put("ret", -1);
            map.put("message", e.getMessage());
        }

        return JSONObject.fromObject(map).toString();
    }

    @ApiOperation("看过我动态的人数")
    @RequestMapping(value = "/new-visitors-count", method = {RequestMethod.GET, RequestMethod.POST})
    public String visitMyDynamicsNum(HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo = getUserLogin(request);
        if (userBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                    ERRORCODE.ACCOUNT_OFF_LINE.getReason());
        }
        logger.info("@RequestMapping(value = \"/new-visitors-count\")=====user:{}({})", userBo.getUserName(),
                userBo.getId());

        List<UserVisitBo> visitBos = userService.visitToMeList(userBo.getId(), 1, 1, Integer.MAX_VALUE);
        // 保存所有访问者的id
        Set<String> temp = new HashSet<>();
        int new_visit_num = 0;

        for (UserVisitBo visitBo : visitBos) {
            UserBo user = userService.getUser(visitBo.getVisitid());
            if (user != null) {
                // 过滤掉想要隐藏的人
                HomepageBo selectByUserId = homepageService.selectByUserId(user.getId());
                if (selectByUserId != null && selectByUserId.getHide_record_set().contains(userBo.getId())) {
                    continue;
                }
                boolean new_read = !visitBo.isRead();
                if (!temp.contains(user.getId())) {
                    temp.add(user.getId());
                    if (new_read) {
                        new_visit_num++;
                    }
                }
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("all_visitors_count", temp.size());
        map.put("new_visitors_count", new_visit_num);
        return JSONObject.fromObject(map).toString();
    }

    @ApiOperation("设置访问不隐身")
    @RequestMapping(value = "/set-not-hide", method = {RequestMethod.GET, RequestMethod.POST})
    public String setNotHide(String uid, HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo = getUserLogin(request);
        if (userBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                    ERRORCODE.ACCOUNT_OFF_LINE.getReason());
        }
        logger.info("@RequestMapping(value = \"/set-not-hide\")=====user:{}({}),uid:{}", userBo.getUserName(),
                userBo.getId(), uid);
        HomepageBo homepageBo = homepageService.selectByUserId(userBo.getId());
        if (homepageBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_ID.getIndex(), ERRORCODE.ACCOUNT_ID.getReason());
        }
        HashSet<String> hide_record_set = homepageBo.getHide_record_set() == null ? new HashSet<String>()
                : homepageBo.getHide_record_set();
        if (hide_record_set.contains(uid)) {
            hide_record_set.remove(uid);
        }
        homepageService.update_hide_record_set(homepageBo.getId(), hide_record_set);
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        return JSONObject.fromObject(map).toString();
    }

    @ApiOperation("设置隐身访问")
    @RequestMapping(value = "/set-hide-me", method = {RequestMethod.GET, RequestMethod.POST})
    public String setHideMe(String uid, HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo = getUserLogin(request);
        if (userBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                    ERRORCODE.ACCOUNT_OFF_LINE.getReason());
        }
        logger.info("@RequestMapping(value = \"/set-hide-me\")=====user:{}({}),uid:{}", userBo.getUserName(),
                userBo.getId(), uid);
        HomepageBo homepageBo = homepageService.selectByUserId(userBo.getId());
        if (homepageBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_ID.getIndex(), ERRORCODE.ACCOUNT_ID.getReason());
        }
        HashSet<String> hide_record_set = homepageBo.getHide_record_set() == null ? new HashSet<String>()
                : homepageBo.getHide_record_set();
        hide_record_set.add(uid);
        homepageService.update_hide_record_set(homepageBo.getId(), hide_record_set);
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        return JSONObject.fromObject(map).toString();
    }

    @ApiOperation("设置用户访问为通知")
    @RequestMapping(value = "/set-allow-push", method = {RequestMethod.GET, RequestMethod.POST})
    public String setAllowPush(String uid, HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo = getUserLogin(request);
        if (userBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                    ERRORCODE.ACCOUNT_OFF_LINE.getReason());
        }
        logger.info("@RequestMapping(value = \"/set-allow-push\")=====user:{}({}),uid:{}", userBo.getUserName(),
                userBo.getId(), uid);
        HomepageBo homepageBo = homepageService.selectByUserId(userBo.getId());
        if (homepageBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_ID.getIndex(), ERRORCODE.ACCOUNT_ID.getReason());
        }
        HashSet<String> not_push_set = homepageBo.getNot_push_set() == null ? new HashSet<String>()
                : homepageBo.getNot_push_set();
        if (not_push_set.contains(uid)) {
            not_push_set.remove(uid);
        }
        homepageService.update_not_push_set(homepageBo.getId(), not_push_set);
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        return JSONObject.fromObject(map).toString();
    }

    @ApiOperation("设置用户访问不通知")
    @RequestMapping(value = "/set-not-push", method = {RequestMethod.GET, RequestMethod.POST})
    public String setNotPush(String uid, HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo = getUserLogin(request);
        if (userBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                    ERRORCODE.ACCOUNT_OFF_LINE.getReason());
        }
        logger.info("@RequestMapping(value = \"/set-not-push\")=====user:{}({}),uid:{}", userBo.getUserName(),
                userBo.getId(), uid);
        HomepageBo homepageBo = homepageService.selectByUserId(userBo.getId());
        if (homepageBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_ID.getIndex(), ERRORCODE.ACCOUNT_ID.getReason());
        }
        HashSet<String> not_push_set = homepageBo.getNot_push_set() == null ? new HashSet<String>()
                : homepageBo.getNot_push_set();
        not_push_set.add(uid);
        homepageService.update_not_push_set(homepageBo.getId(), not_push_set);
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        return JSONObject.fromObject(map).toString();
    }

    @ApiOperation("删除来访记录(谁看过我)")
    @RequestMapping(value = "/delete-vzt2me-history", method = {RequestMethod.GET, RequestMethod.POST})
    public String deleteVisit2Me(String uid, HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo = getUserLogin(request);
        if (userBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                    ERRORCODE.ACCOUNT_OFF_LINE.getReason());
        }
        logger.info("@RequestMapping(value = \"/delete-vzt2me-history\"),user:{},userid:{},uid:{}",
                userBo.getUserName(), userBo.getId(), uid);
        userService.deleteByVisitid(uid, userBo.getId());
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        return JSONObject.fromObject(map).toString();
    }

    @ApiOperation("删除来访记录(我看过谁)")
    @RequestMapping(value = "/delete-i2vzt-history", method = {RequestMethod.GET, RequestMethod.POST})
    public String deleteMe2Visit(String uid, HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo = getUserLogin(request);
        if (userBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                    ERRORCODE.ACCOUNT_OFF_LINE.getReason());
        }
        logger.info("@RequestMapping(value = \"/delete-i2vzt-history\"),user:{},userid:{},uid:{}", userBo.getUserName(),
                userBo.getId(), uid);
        userService.deleteByVisitid(userBo.getId(), uid);
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        return JSONObject.fromObject(map).toString();
    }

    @ApiOperation("添加动态信息")
    @PostMapping(value = "/insert")
    public String insert(String paramString, MultipartFile[] pictures, HttpServletRequest request, HttpServletResponse response) {
        logger.info("@PostMapping(value = \"/insert\")=====json:{}", paramString);
        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(paramString);
        double px = jsonObject.getDouble("px");
        double py = jsonObject.getDouble("py");
        String landmark = jsonObject.getString("landmark");
        String content = jsonObject.getString("content");
        String type = jsonObject.getString("type");
        Boolean ishide = jsonObject.getBoolean("ishide");
        List<String> list = (List<String>) jsonObject.get("atIds");
        LinkedHashSet<String> atIds = new LinkedHashSet<>(list);
        return insert(px, py, null, content, landmark, pictures, type, atIds, ishide, request, response);
    }

    @ApiOperation("所有好友动态列表")
    @RequestMapping(value = "/all-dynamics", method = {RequestMethod.GET, RequestMethod.POST})
    public String allDynamics(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        logger.info("@RequestMapping(value = \"/all-dynamics\")=====user:{}({}),page:{},limit:{}", userBo.getUserName(),
                userBo.getId(), page, limit);
        List<String> friends = CommonUtil.deleteBack(dynamicService, friendsService, userBo);
        friends.add(userBo.getId());
        System.out.println("friends:"+friends.toString());
        List<DynamicBo> msgBos = dynamicService.findAllFriendsMsg(friends, page, limit);
        List<DynamicVo> dynamicVos = new ArrayList<>();
        dynamicBo2vo(msgBos, dynamicVos, userBo);
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("dynamicVos", dynamicVos);
        map.put("backPic", userBo.getDynamicPic());
        map.put("headPic", userBo.getHeadPictureName());
        map.put("signature", userBo.getPersonalizedSignature());
        return JSONObject.fromObject(map).toString();
    }

    @ApiOperation("获取所有好友动态数量")
    @RequestMapping(value = "/all-dynamics-num", method = {RequestMethod.GET, RequestMethod.POST})
    public String allFriendsNum(HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        logger.info("@RequestMapping(value = \"/all-dynamics-num\")=====user:{}({})", userBo.getUserName(),
                userBo.getId());
        List<String> friends = CommonUtil.deleteBack(dynamicService, friendsService, userBo);
        friends.add(userBo.getId());
        List<DynamicBo> msgBos = dynamicService.findAllFriendsMsg(friends, -1, 0);
        long notReadNum = dynamicService.findDynamicNotReadNum(userBo.getId());
        int hideNum = getHideNum(userBo, msgBos);

        int total = msgBos != null ? msgBos.size() : 0;
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("dynamicNum", total - hideNum);
        map.put("notReadNum", (notReadNum - hideNum) > 0 ? (notReadNum - hideNum) : 0);
        return JSONObject.fromObject(map).toString();
    }

    @ApiOperation("好友动态列表")
    @RequestMapping(value = "/friend-dynamics", method = {RequestMethod.GET, RequestMethod.POST})
    public String allDynamics(String friendid, int page, int limit, HttpServletRequest request,
                              HttpServletResponse response) {
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        logger.info("@RequestMapping(value = \"/friend-dynamics\")=====user:{}({}),friendid:{},page:{},limit:{}",
                userBo.getUserName(), userBo.getId(), friendid, page, limit);
        UserBo friend = userService.getUser(friendid);
        if (friend == null) {
            return CommonUtil.toErrorResult(ERRORCODE.FRIEND_NULL.getIndex(), ERRORCODE.FRIEND_NULL.getReason());
        }
        addVisitHis(userBo.getId(), friendid);
        List<DynamicBo> msgBos = dynamicService.findOneFriendMsg(friendid, page, limit);
        List<DynamicVo> dynamicVos = new ArrayList<>();
        dynamicBo2vo(msgBos, dynamicVos, userBo);
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("dynamicVos", dynamicVos);
        map.put("backPic", friend.getDynamicPic());
        map.put("headPic", friend.getHeadPictureName());
        map.put("signature", friend.getPersonalizedSignature());
        return JSONObject.fromObject(map).toString();
    }

    @ApiOperation(" 获取好友动态数量")
    @RequestMapping(value = "/one-dynamics-num", method = {RequestMethod.GET, RequestMethod.POST})
    public String friendsNum(String friendid, HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo = getUserLogin(request);
        if (userBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                    ERRORCODE.ACCOUNT_OFF_LINE.getReason());
        }
        logger.info("@RequestMapping(value = \"/one-dynamics-num\")=====user:{}({}),friendid:{}", userBo.getUserName(),
                userBo.getId(), friendid);
        List<DynamicBo> msgBos = dynamicService.findOneFriendMsg(friendid, -1, 0);

        int hideNum = getHideNum(userBo, msgBos);

        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("dynamicNum", msgBos == null ? 0 : ((msgBos.size() - hideNum) < 0 ? 0 : (msgBos.size() - hideNum)));
        return JSONObject.fromObject(map).toString();
    }

    private int getHideNum(UserBo userBo, List<DynamicBo> msgBos) {
        int hideNum = 0;
        for (DynamicBo dynamicBo : msgBos) {
            if(userBo.getId().equals(dynamicBo.getCreateuid())){
                continue;
            }
            if (dynamicBo.getAccess_level() == UserCenterConstants.ACCESS_SECURITY_ALLOW_NONE) {
                hideNum++;
            }
            if (dynamicBo.getAccess_level() == UserCenterConstants.ACCESS_SECURITY_ALLOW_PART) {
                if (!dynamicBo.getAccess_allow_set().contains(userBo.getId())) {
                    hideNum++;
                }
            }
        }
        return hideNum;
    }

    @ApiOperation("我的动态")
    @RequestMapping(value = "/my-dynamics", method = {RequestMethod.GET, RequestMethod.POST})
    public String myDynamics(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo = getUserLogin(request);
        if (userBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                    ERRORCODE.ACCOUNT_OFF_LINE.getReason());
        }
        logger.info("@RequestMapping(value = \"/my-dynamics\")=====user:{}({}),page:{},limit:{}",
                userBo.getUserName(), userBo.getId(), page, limit);
        List<DynamicBo> msgBos = dynamicService.findOneFriendMsg(userBo.getId(), page, limit);


        List<DynamicVo> dynamicVos = new ArrayList<>();
        dynamicBo2vo(msgBos, dynamicVos, userBo);
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("dynamicVos", dynamicVos);
        map.put("backPic", userBo.getDynamicPic() == null ? "" : userBo.getDynamicPic());
        map.put("headPic", userBo.getHeadPictureName());
        map.put("signature", userBo.getPersonalizedSignature());
        HomepageBo homepageBo = homepageService.selectByUserId(userBo.getId());
        if(homepageBo == null){
            homepageBo = new HomepageBo();
            homepageBo.setOwner_id(userBo.getId());
            homepageService.insert(homepageBo);
        }
        HashSet<String> not_push_set = homepageBo.getNot_push_set();

        List<UserVisitBo> userVisitBos = userService.findUserVisitFirst(userBo.getId(), not_push_set, 1);

        UserBaseVo show = new UserBaseVo();
        for (UserVisitBo userVisitBo : userVisitBos) {
            HomepageBo ownerHomePage = homepageService.selectByUserId(userVisitBo.getOwnerid());
            HashSet<String> hide_record_set = ownerHomePage.getHide_record_set();

            if (userVisitBo != null && !hide_record_set.contains(userVisitBo.getOwnerid())) {
                UserBo user = userService.getUser(userVisitBo.getVisitid());
                if (user != null) {
                    BeanUtils.copyProperties(user, show);
                }
                break;
            }
        }

        map.put("showUser", show);
//        return JSON.toJSONString(map).replace("\\", "").replace("\"{", "{").replace("}\"", "}");
        return JSONObject.fromObject(map).toString();
    }

    @ApiOperation("我的动态数量")
    @RequestMapping(value = "/my-dynamics-num", method = {RequestMethod.GET, RequestMethod.POST})
    public String myDynamicsNum(HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo = getUserLogin(request);
        if (userBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                    ERRORCODE.ACCOUNT_OFF_LINE.getReason());
        }
        logger.info("@RequestMapping(value = \"/my-dynamics-num\")=====user:{}({})", userBo.getUserName(),
                userBo.getId());

        List<DynamicBo> msgBos = dynamicService.findOneFriendMsg(userBo.getId(), -1, 0);
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("dynamicNum", msgBos == null ? 0 : msgBos.size());
        return JSONObject.fromObject(map).toString();
    }

    @ApiOperation("不看他的动态")
    @RequestMapping(value = "/dynamic-not-see", method = {RequestMethod.GET, RequestMethod.POST})
    public String notSee(String friendid, HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo = getUserLogin(request);
        if (userBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                    ERRORCODE.ACCOUNT_OFF_LINE.getReason());
        }
        logger.info("@RequestMapping(value = \"/dynamic-not-see\")=====user:{}({}),friendid:{}", userBo.getUserName(),
                userBo.getId(), friendid);
        DynamicBackBo backBo = dynamicService.findBackByUserid(userBo.getId());
        if (backBo == null) {
            backBo = new DynamicBackBo();
            backBo.setUserid(userBo.getId());
            HashSet<String> notSees = backBo.getNotSeeBacks();
            notSees.add(friendid);
            backBo.setNotSeeBacks(notSees);
            dynamicService.addDynamicBack(backBo);
        } else {
            HashSet<String> notSees = backBo.getNotSeeBacks();
            notSees.add(friendid);
            dynamicService.updateBackNotSee(backBo.getId(), notSees);
        }
        return Constant.COM_RESP;
    }

    @ApiOperation("谁看过我的动态")
    @RequestMapping(value = "/visit-my-dynamic", method = {RequestMethod.GET, RequestMethod.POST})
    public String visitMyDynamics(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo = getUserLogin(request);
        if (userBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                    ERRORCODE.ACCOUNT_OFF_LINE.getReason());
        }
        logger.info("@RequestMapping(value = \"/visit-my-dynamic\")=====user:{}({}),page:{},limit",
                userBo.getUserName(), userBo.getId(), page, limit);

        List<UserVisitBo> visitBos = userService.visitToMeList(userBo.getId(), 1, page, limit);
        List<Object> visitUsers = new LinkedList<>();
        // 保存所有访问者的id
        Set<String> temp = new HashSet<>();

        // 获取不通知列表
        HomepageBo myHomepage = homepageService.selectByUserId(userBo.getId());
        HashSet<String> not_push_set = myHomepage.getNot_push_set() == null ? new HashSet<>()
                : myHomepage.getNot_push_set();

        for (UserVisitBo visitBo : visitBos) {
            UserBo user = userService.getUser(visitBo.getVisitid());

            if (user != null) {
                // 过滤掉想要隐藏的人
                HomepageBo selectByUserId = homepageService.selectByUserId(user.getId());
                if (selectByUserId != null && selectByUserId.getHide_record_set().contains(userBo.getId())) {
                    continue;
                }

                if (!temp.contains(user.getId())) {
                    UserBaseVo baseVo = new UserBaseVo();
                    BeanUtils.copyProperties(user, baseVo);
                    List<UserVisitBo> visBos = userService.visitToMeList(userBo.getId(), user.getId(), 1);
                    List<Date> visitTime = new ArrayList<>();
                    for (UserVisitBo date : visBos) {
                        if (date != null) {
                            if (date.getVisitTime() != null) {
                                visitTime.add(date.getVisitTime());
                            }
                        }
                    }
                    com.alibaba.fastjson.JSONObject parseObject = JSON.parseObject(JSON.toJSONString(baseVo));
                    parseObject.put("visitTime", visitTime);
                    parseObject.put("push", !not_push_set.contains(user.getId()));
                    temp.add(user.getId());
                    visitUsers.add(parseObject);
                }
            }
        }

        dynamicService.updateReadToTure(userBo.getId(), temp);
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("visitUserVos", visitUsers);
        return JSONObject.fromObject(map).toString();
    }

    @ApiOperation("我看过谁的动态")
    @RequestMapping(value = "/my-visit-dynamic", method = {RequestMethod.GET, RequestMethod.POST})
    public String myVisitDynamics(int page, int limit, HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo = getUserLogin(request);
        if (userBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                    ERRORCODE.ACCOUNT_OFF_LINE.getReason());
        }
        logger.info("@RequestMapping(value = \"/my-visit-dynamic\")=====user:{}({}),page:{},limit",
                userBo.getUserName(), userBo.getId(), page, limit);
        List<UserVisitBo> visitBos = userService.visitFromMeList(userBo.getId(), 1, page, limit);
        List<Object> visitUsers = new LinkedList<>();
        Set<String> temp = new HashSet<>();
        HomepageBo myHomepage = homepageService.selectByUserId(userBo.getId());
        HashSet<String> hide_record_set = myHomepage.getHide_record_set() == null ? new HashSet<String>()
                : myHomepage.getHide_record_set();
        ;
        for (UserVisitBo visitBo : visitBos) {
            UserBo user = userService.getUser(visitBo.getOwnerid());
            if (user != null) {
                if (!temp.contains(user.getId())) {
                    UserBaseVo baseVo = new UserBaseVo();
                    BeanUtils.copyProperties(user, baseVo);
                    List<UserVisitBo> visBos = userService.visitToMeList(user.getId(), userBo.getId(), 1);
                    List<Date> visitTime = new ArrayList<>();
                    for (UserVisitBo date : visBos) {
                        if (date != null) {
                            if (date.getVisitTime() != null) {
                                visitTime.add(date.getVisitTime());
                            }
                        }
                    }
                    com.alibaba.fastjson.JSONObject parseObject = JSON.parseObject(JSON.toJSONString(baseVo));
                    parseObject.put("visitTime", visitTime);
                    parseObject.put("hide", hide_record_set.contains(user.getId()));
                    temp.add(user.getId());
                    visitUsers.add(parseObject);
                }

            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("visitUserVos", visitUsers);
        return JSONObject.fromObject(map).toString();
    }

    @ApiOperation("更新动态背景图片")
    @RequestMapping(value = "/update-backpic", method = {RequestMethod.GET, RequestMethod.POST})
    public String updateDynamicsPic(MultipartFile backPic, HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo = getUserLogin(request);
        if (userBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
                    ERRORCODE.ACCOUNT_OFF_LINE.getReason());
        }
        logger.info("@RequestMapping(value = \"/update-backpic\")=====user:{}({})", userBo.getUserName(),
                userBo.getId());

        if (backPic != null) {
            Long time = Calendar.getInstance().getTimeInMillis();
            String fileName = String.format("%s-%d-%s", userBo.getId(), time, backPic.getOriginalFilename());
            String path = CommonUtil.upload(backPic, Constant.DYNAMIC_PICTURE_PATH, fileName, 0);
            userService.updateUserDynamicPic(userBo.getId(), path);
            userBo.setDynamicPic(path);
            request.getSession().setAttribute("userBo", userBo);
        }
        return Constant.COM_RESP;
    }

    /**
     * 访问记录添加
     *
     * @param userid
     * @param friendid
     */
    private void addVisitHis(String userid, String friendid) {
        UserVisitBo visitBo = new UserVisitBo();
        visitBo.setVisitTime(new Date());
        visitBo.setVisitid(userid);
        visitBo.setOwnerid(friendid);
        visitBo.setType(1);
        HomepageBo selectByUserId = homepageService.selectByUserId(userid);
        if (selectByUserId != null && selectByUserId.getHide_record_set().contains(friendid)) {
            visitBo.setRead(true);
        }
        userService.addUserVisit(visitBo);
    }

    /**
     * 添加动态
     *
     * @param px
     * @param py
     * @param title
     * @param content
     * @param landmark
     * @param pictures
     * @param type
     * @param request
     * @param response
     * @return
     */
    private String insert(double px, double py, String title, String content, String landmark, MultipartFile[] pictures,
                          String type, LinkedHashSet<String> atIds, boolean ishide, HttpServletRequest request,
                          HttpServletResponse response) {
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        logger.info(
                "private insert=====user:{}({}),position:[{},{}],title:{},content:{},landmark:{},type:{},atIds:{}",
                userBo.getUserName(), userBo.getId(), px, py, title, content, landmark, type, JSON.toJSONString(atIds));
        String userId = userBo.getId();
        DynamicBo dynamicBo = new DynamicBo();

        dynamicBo.setTitle(title == null ? "动态未命名" : title);
        dynamicBo.setContent(content);
        dynamicBo.setCreateuid(userId);

        dynamicBo.setLandmark(landmark);
        dynamicBo.setPostion(new double[]{px, py});
        dynamicBo.setAtIds(atIds);

        if (pictures != null) {
            dynamicBo.setFileType(type);
            LinkedHashSet<String> images = dynamicBo.getPhotos();
            for (MultipartFile file : pictures) {
                Long time = Calendar.getInstance().getTimeInMillis();
                String fileName = String.format("%s-%d-%s", userId, time, file.getOriginalFilename());
                if ("video".equals(type)) {
                    // 0 是video 的路径， 1 是缩略图路径
                    String[] paths = CommonUtil.uploadVedio(file, Constant.DYNAMIC_PICTURE_PATH, fileName, 0);
                    // images.add(paths[0]);
                    dynamicBo.setVideo(paths[0]);
                    dynamicBo.setVideoPic(paths[1]);
                } else {
                    String path = CommonUtil.upload(file, Constant.DYNAMIC_PICTURE_PATH, fileName, 0);
                    images.add(path);
                }
            }
            dynamicBo.setPhotos(images);
        }

        if (ishide) {
            dynamicBo.setAccess_level(UserCenterConstants.ACCESS_SECURITY_ALLOW_NONE);
        }

        dynamicService.addDynamic(dynamicBo);

        // 防止影响后续返回
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncPush(atIds, userBo, userId, dynamicBo);
            }
        }).start();

        updateDynamicNums(userId, 1, dynamicService, redisServer);
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("dynamicid", dynamicBo.getId());
        return JSONObject.fromObject(map).toString();
    }

    private void syncPush(LinkedHashSet<String> atIds, UserBo userBo, String userId, DynamicBo dynamicBo) {
        synchronized (DynamicController.class){
            List<String> pushIds = atIds.parallelStream()
                    .filter(atid -> homepageService.selectByUserId(atid) != null && !homepageService.selectByUserId(atid).getNot_push_set().contains(userId))
                    .collect(Collectors.toList());
            String path = new String("/dynamic/dynamic_info?dynamicId=" + dynamicBo.getId());
            String pushContent = String.format("“%s”在Ta的个人动态中@了你，快去看看吧！", userBo.getUserName());
            usePush(pushIds, pushTitle, pushContent, path);
//            addCrcular(pushIds, pushTitle, pushContent, path);
        }
    }
}
