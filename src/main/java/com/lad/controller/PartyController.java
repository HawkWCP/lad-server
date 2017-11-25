package com.lad.controller;

import com.lad.bo.*;
import com.lad.redis.RedisServer;
import com.lad.service.*;
import com.lad.util.*;
import com.lad.vo.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.api.RLock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 功能描述：
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/9/7
 */
@Controller
@RequestMapping("/party")
public class PartyController extends BaseContorller {

    private static Logger logger = LogManager.getLogger(PartyController.class);

    @Autowired
    private IPartyService partyService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ICircleService circleService;

    @Autowired
    private ICommentService commentService;

    @Autowired
    private IThumbsupService thumbsupService;
    
    @Autowired
    private RedisServer redisServer;

    @Autowired
    private IDynamicService dynamicService;

    @Autowired
    private IChatroomService chatroomService;

    @Autowired
    private ICollectService collectService;

    @Autowired
    private IFeedbackService feedbackService;

    private int dayTimeMins = 24 * 60 * 60 * 1000;

    private String titlePush = "聚会通知";


    @RequestMapping("/create")
    @ResponseBody
    public String create(@RequestParam String partyJson, MultipartFile backPic,
                          MultipartFile[] photos, MultipartFile video,
                          HttpServletRequest request, HttpServletResponse response){

        logger.info("partyJson : {}",partyJson);
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        PartyBo partyBo = null;
        try {
            JSONObject jsonObject = JSONObject.fromObject(partyJson);
            partyBo = (PartyBo)JSONObject.toBean(jsonObject, PartyBo.class);
        } catch (Exception e) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_ERROR.getIndex(),
                    ERRORCODE.PARTY_ERROR.getReason());
        }
        CircleBo circleBo = circleService.selectById(partyBo.getCircleid());
        if (circleBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_IS_NULL.getIndex(),
                    ERRORCODE.CIRCLE_IS_NULL.getReason());
        }
        String userId = userBo.getId();
        Long time = Calendar.getInstance().getTimeInMillis();
        if (photos != null) {
            LinkedHashSet<String> photo = new LinkedHashSet<>();
            for (MultipartFile file : photos) {
                String fileName = userId + "-" + time + "-" + file.getOriginalFilename();
                String path = CommonUtil.upload(file, Constant.PARTY_PICTURE_PATH,
                        fileName, 0);
                photo.add(path);
            }
            partyBo.setPhotos(photo);
        }
        if (video != null) {
            try {
                String fileName = userId + "-" + time + "-" + video.getOriginalFilename();
                logger.info("---- party file: {} ,  size: {}" , video.getOriginalFilename(), video.getSize());
                String[] paths = CommonUtil.uploadVedio(video, Constant.PARTY_PICTURE_PATH, fileName, 0);
                partyBo.setVideo(paths[0]);
                partyBo.setVideoPic(paths[1]);
            } catch (Exception e) {
                logger.error(e);
            }
        }
        if (backPic != null) {
            String fileName = userId + "-" + time + "-" + backPic.getOriginalFilename();
            String path =  CommonUtil.upload(backPic, Constant.PARTY_PICTURE_PATH, fileName, 0);
            partyBo.setBackPic(path);
        }
        partyBo.setStatus(1);
        partyBo.setCreateuid(userId);
        LinkedList<String> partyUsers = partyBo.getUsers();
        partyUsers.add(userId);
        partyBo.setPartyUserNum(1);

        ChatroomBo chatroomBo = new ChatroomBo();
        chatroomBo.setName(partyBo.getTitle());
        chatroomBo.setType(Constant.ROOM_MULIT);
        chatroomBo.setCreateuid(userBo.getId());
        chatroomBo.setMaster(userBo.getId());
        chatroomBo.setOpen(true);
        chatroomBo.setVerify(false);
        HashSet<String> users = chatroomBo.getUsers();
        users.add(userId);
        chatroomService.insert(chatroomBo);
        //第一个为返回结果信息，第二位term信息
        String result = IMUtil.subscribe(0, chatroomBo.getId(), userBo.getId());
        if (!result.equals(IMUtil.FINISH)) {
            chatroomService.remove(chatroomBo.getId());
            return result;
        }
        userService.updateChatrooms(userBo);
        addChatroomUser(chatroomService, userBo, chatroomBo.getId(), userBo.getUserName());
        partyBo.setChatroomid(chatroomBo.getId());
        partyService.insert(partyBo);
        HashSet<String> circleUsers = circleBo.getUsers();

        if (circleUsers.size() > 0) {
            String path = "/party/party-info.do?partyid=" + partyBo.getId();
            String content = String.format("%s发起了聚会【%s】，快去看看吧", userBo.getUserName(),
                    partyBo.getTitle());
            String[] userids = new String[circleUsers.size()];
            circleUsers.toArray(userids);
            JPushUtil.push(titlePush, content, path, userids);
        }

        //用户等级
        userService.addUserLevel(userBo.getId(), 1, Constant.PARTY_TYPE);
        //圈子热度
        updateCircleHot(circleService, redisServer, partyBo.getCircleid(), 1, Constant.CIRCLE_PARTY_VISIT);
        //动态信息表
        addDynamicMsgs(userId, partyBo.getId(), Constant.PARTY_TYPE, dynamicService);
        updateDynamicNums(userId, 1, dynamicService, redisServer);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ret", 0);
        map.put("partyid", partyBo.getId());
        return JSONObject.fromObject(map).toString();
    }


    /**
     * 更新聚会信息
     * @param partyid
     * @param partyJson
     * @param backPic
     * @param photos
     * @param delPhotos
     * @param video
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/update")
    @ResponseBody
    public String update(@RequestParam String partyid, String partyJson, String delPhotos,
                         MultipartFile backPic, MultipartFile[] photos, MultipartFile video,
                         HttpServletRequest request, HttpServletResponse response){

        logger.info("update partyJson : {}",partyJson);

        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        PartyBo partyBo = null;
        try {
            if (StringUtils.isNotEmpty(partyJson)) {
                JSONObject jsonObject = JSONObject.fromObject(partyJson);
                partyBo = (PartyBo)JSONObject.toBean(jsonObject, PartyBo.class);
            }
        } catch (Exception e) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_ERROR.getIndex(),
                    ERRORCODE.PARTY_ERROR.getReason());
        }
        PartyBo oldParty = partyService.findById(partyid);
        if (oldParty == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_NULL.getIndex(),
                    ERRORCODE.PARTY_NULL.getReason());
        }
        if (partyBo == null){
            partyBo = oldParty;
        } else {
            copyOld(oldParty, partyBo);
            partyBo.setPhotos(oldParty.getPhotos());
            partyBo.setUsers(oldParty.getUsers());
        }
        String userId = userBo.getId();
        Long time = Calendar.getInstance().getTimeInMillis();
        if (photos != null) {
            LinkedHashSet<String> photo = partyBo.getPhotos();
            for (MultipartFile file : photos) {
                String fileName = userId + "-" + time + "-" + file.getOriginalFilename();
                String path = CommonUtil.upload(file, Constant.PARTY_PICTURE_PATH,
                        fileName, 0);
                photo.add(path);
            }
        }
        if (StringUtils.isNotEmpty(delPhotos)) {
            LinkedHashSet<String> photo = partyBo.getPhotos();
            String[] paths = CommonUtil.getIds(delPhotos);
            for (String url : paths) {
                if (photo.contains(url)){
                    photo.remove(url);
                }
            }
        }
        if (video != null) {
            String fileName = userId + "-" + time + "-" + video.getOriginalFilename();
            String[] paths = CommonUtil.uploadVedio(video, Constant.PARTY_PICTURE_PATH, fileName, 0);
            partyBo.setVideo(paths[0]);
            partyBo.setVideoPic(paths[1]);
        }
        if (backPic != null) {
            String fileName = userId + "-" + time + "-" + backPic.getOriginalFilename();
            String path =  CommonUtil.upload(backPic, Constant.PARTY_PICTURE_PATH, fileName, 0);
            partyBo.setBackPic(path);
        }
        partyService.update(partyBo);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ret", 0);
        map.put("partyid", partyBo.getId());
        return JSONObject.fromObject(map).toString();
    }


    @RequestMapping("/party-info")
    @ResponseBody
    public String manageParty(@RequestParam String partyid,
                         HttpServletRequest request, HttpServletResponse response){

        PartyBo partyBo = partyService.findById(partyid);
        if (partyBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_NULL.getIndex(),
                    ERRORCODE.PARTY_NULL.getReason());
        }
        UserBo userBo = getUserLogin(request);
        String userid = userBo != null ? userBo.getId() : "";

        CircleBo circleBo = circleService.selectById(partyBo.getCircleid());
        if (circleBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_IS_NULL.getIndex(),
                    ERRORCODE.CIRCLE_IS_NULL.getReason());
        }
        List<PartyUserVo> partyUserVos = new ArrayList<>();
        LinkedList<String> users = partyBo.getUsers();
        int length = users.size() -1;
        int userAdd = 0;
        for (int i = length; i >=0 ; i--) {
            UserBo user =  userService.getUser(users.get(i));
            if (userAdd > 10) {
                break;
            }
            if (user !=null) {
                PartyUserVo userVo = new PartyUserVo();
                partyUserBo2Vo(user, userVo);
                partyUserVos.add(userVo);
                userAdd ++;
            }
        }
        PartyVo partyVo = new PartyVo();
        BeanUtils.copyProperties(partyBo, partyVo);
        if (partyBo.getStatus() != -1){
            int status = getPartyStatus(partyBo.getStartTime(), partyBo.getAppointment());
            if (status != partyBo.getStatus()){
                updatePartyStatus(partyBo.getId(), status);
                partyBo.setStatus(status);
            }
        }
        partyVo.setPartyid(partyBo.getId());
        partyVo.setCircleName(circleBo.getName());
        partyVo.setCirclePic(circleBo.getHeadPicture());
        partyVo.setInCircle(circleBo.getUsers().contains(userid));
        partyVo.setUsers(partyUserVos);
        partyVo.setCreate(partyBo.getCreateuid().equals(userid));
        UserBo createBo = userService.getUser(partyBo.getCreateuid());
        if (createBo != null) {
            PartyUserVo createVo = new PartyUserVo();
            partyUserBo2Vo(createBo, createVo);
            partyVo.setCreater(createVo);
        }
        if (StringUtils.isNotEmpty(userid)){
            PartyUserBo partyUserBo = partyService.findPartyUserIgnoreDel(partyid, userid);
            partyVo.setInParty(partyUserBo != null && partyUserBo.getDeleted() == 0);
            partyVo.setCollect(partyUserBo != null && partyUserBo.getCollectParty() == 1);
            List<CommentBo> commentBos = commentService.selectByTargetUser(partyid, userid, Constant.PARTY_TYPE);
            partyVo.setComment(commentBos != null && !commentBos.isEmpty());
        }
        partyVo.setUserNum(partyBo.getPartyUserNum());
        partyService.updateVisit(partyid);
        //圈子热度
        updateCircleHot(circleService, redisServer, partyBo.getCircleid(), 1, Constant.CIRCLE_PARTY_VISIT);

        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("partyVo", partyVo);
        return JSONObject.fromObject(map).toString();
    }

    private void partyUserBo2Vo(UserBo userBo, PartyUserVo userVo){
        userVo.setUserPic(userBo.getHeadPictureName());
        userVo.setUsername(userBo.getUserName());
        userVo.setUserid(userBo.getId());
    }

    /**
     * 报名聚会
     * @param partyid
     * @return
     */
    @RequestMapping("/enroll-party")
    @ResponseBody
    public String enrollParty(String partyid,String phone, String joinInfo, int userNum, double amount,
                            HttpServletRequest request, HttpServletResponse response){
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }

        RLock lock = redisServer.getRLock(partyid+"partyUserLock");
        String chatroomid;
        PartyBo partyBo = null;
        try {
            lock.lock(2, TimeUnit.SECONDS);
            partyBo = partyService.findById(partyid);
            if (partyBo == null) {
                return CommonUtil.toErrorResult(ERRORCODE.PARTY_NULL.getIndex(),
                        ERRORCODE.PARTY_NULL.getReason());
            }
            if (partyBo.getStatus() == 2) {
                return CommonUtil.toErrorResult(ERRORCODE.PARTY_HAS_END.getIndex(),
                        ERRORCODE.PARTY_HAS_END.getReason());
            }
            int userTotal = partyBo.getPartyUserNum() + userNum;
            if (partyBo.getUserLimit() != 0 && userTotal > partyBo.getUserLimit()) {
                return CommonUtil.toErrorResult(ERRORCODE.PARTY_USER_MAX.getIndex(),
                        ERRORCODE.PARTY_USER_MAX.getReason());
            }
            partyBo.setPartyUserNum(userTotal);
            chatroomid = partyBo.getChatroomid();
            LinkedList<String> users = partyBo.getUsers();
            if (!users.contains(userBo.getId())) {
                users.add(userBo.getId());
                partyService.updateUser(partyid, users, userTotal);
            } else {
                return CommonUtil.toErrorResult(ERRORCODE.PARTY_HAS_ADD.getIndex(),
                        ERRORCODE.PARTY_HAS_ADD.getReason());
            }
        } finally {
            lock.unlock();
        }
        String userid = userBo.getId();
        PartyUserBo partyUserBo = partyService.findPartyUserIgnoreDel(partyid, userid);
        if (partyUserBo == null) {
            partyUserBo = new PartyUserBo();
            partyUserBo.setAmount(amount);
            partyUserBo.setJoinInfo(joinInfo);
            partyUserBo.setJoinPhone(phone);
            partyUserBo.setUserid(userBo.getId());
            partyUserBo.setPartyid(partyid);
            partyUserBo.setUserNum(userNum);
            partyUserBo.setStatus(1);
            partyService.addParty(partyUserBo);
        } else {
            if (partyUserBo.getDeleted() == Constant.DELETED) {
                partyUserBo.setAmount(amount);
                partyUserBo.setJoinInfo(joinInfo);
                partyUserBo.setJoinPhone(phone);
                partyUserBo.setUserNum(userNum);
                partyUserBo.setStatus(1);
                partyUserBo.setDeleted(Constant.ACTIVITY);
                partyService.updatePartyUser(partyUserBo);
            }
        }
        if (StringUtils.isNotEmpty(chatroomid)) {
            ChatroomBo chatroomBo = chatroomService.get(chatroomid);
            LinkedHashSet<String> chatroomUsers = chatroomBo.getUsers();
            if (!chatroomUsers.contains(userid)){
                //第一个为返回结果信息，第二位term信息
                String result = IMUtil.subscribe(1, chatroomid, userid);
                if (!result.equals(IMUtil.FINISH)) {
                    return result;
                }
                HashSet<String> chatroom = userBo.getChatrooms();
                //个人聊天室中没有当前聊天室，则添加到个人的聊天室
                if (!chatroom.contains(chatroomid)) {
                    chatroom.add(chatroomid);
                    userBo.setChatrooms(chatroom);
                    userService.updateChatrooms(userBo);
                }
                chatroomUsers.add(userid);
                chatroomBo.setUsers(chatroomUsers);
                chatroomService.updateUsers(chatroomBo);
            }
        }
        ChatroomUserBo chatroomUserBo = chatroomService.findChatUserByUserAndRoomid(userid, chatroomid);
        if (chatroomUserBo == null){
            chatroomUserBo = new ChatroomUserBo();
            chatroomUserBo.setChatroomid(chatroomid);
            chatroomUserBo.setUserid(userid);
            chatroomUserBo.setUsername(userBo.getUserName());
            chatroomService.insertUser(chatroomUserBo);
        }

        String path = String.format("/party/enroll-detail.do?partyid=%s&userid=%s", partyid, userid);
        String content = String.format("%s报名了您发起的聚会【%s】，请尽快与他沟通参与事宜", userBo.getUserName(),
                partyBo.getTitle());
        JPushUtil.push(titlePush, content, path,  partyBo.getCreateuid());
        
        return Constant.COM_RESP;
    }

    /**
     * 获取报名人员列表
     * @param partyid
     * @return
     */
    @RequestMapping("/get-users")
    @ResponseBody
    public String enrollUsers(String partyid, HttpServletRequest request, HttpServletResponse response){

        PartyBo partyBo = partyService.findById(partyid);
        if (partyBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_NULL.getIndex(),
                    ERRORCODE.PARTY_NULL.getReason());
        }
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        List<PartyUserBo> partyUserBos = partyService.findPartyUser(partyid, 1);
        List<PartyUserVo> partyUserVos = new ArrayList<>();
        for (PartyUserBo partyUserBo : partyUserBos) {
            UserBo user =  userService.getUser(partyUserBo.getUserid());
            if (user !=null) {
                PartyUserVo userVo = new PartyUserVo();
                partyUserBo2Vo(user, userVo);
                partyUserVos.add(userVo);
            } else {
                partyService.outParty(partyUserBo.getId());
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("partyUserVos", partyUserVos);
        return JSONObject.fromObject(map).toString();
    }

    /**
     * 管理聚会
     * @param partyid
     * @return
     */
    @RequestMapping("/manage-enroll")
    @ResponseBody
    public String getEnroll(@RequestParam String partyid,
                           HttpServletRequest request, HttpServletResponse response){
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ret", 0);
        map.put("partyVo", "");
        return JSONObject.fromObject(map).toString();
    }


    /**
     * 获取我发起的聚会
     * @return
     */
    @RequestMapping("/my-partys")
    @ResponseBody
    public String getMyPartys(int page, int limit, HttpServletRequest request, HttpServletResponse response){

        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        List<PartyBo> partyBos = partyService.findByCreate(userBo.getId(), page, limit);
        List<PartyListVo> partyListVos = new ArrayList<>();
        bo2listVo(partyBos, partyListVos);
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("partyListVos", partyListVos);
        return JSONObject.fromObject(map).toString();
    }

    /**
     * 获取我参与的聚会
     * @return
     */
    @RequestMapping("/join-partys")
    @ResponseBody
    public String getJoinPartys(int page, int limit, HttpServletRequest request, HttpServletResponse response){
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        List<PartyBo> partyBos = partyService.findByMyJoin(userBo.getId(), page, limit);
        List<PartyListVo> partyListVos = new ArrayList<>();
        for(PartyBo partyBo : partyBos) {
            //判断用户是否已经删除这个信息
            PartyUserBo partyUserBo = partyService.findPartyUser(partyBo.getId(), userBo.getId());
            if (partyUserBo != null && partyUserBo.getUserDelete() == Constant.DELETED) {
                continue;
            }
            PartyListVo listVo = new PartyListVo();
            BeanUtils.copyProperties(partyBo, listVo);
            listVo.setPartyid(partyBo.getId());
            listVo.setUserNum(partyBo.getPartyUserNum());
            partyListVos.add(listVo);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("partyListVos", partyListVos);
        return JSONObject.fromObject(map).toString();
    }

    private void bo2listVo(List<PartyBo> partyBos, List<PartyListVo> partyListVos){
        for(PartyBo partyBo : partyBos) {
            LinkedHashSet<String> startTimes = partyBo.getStartTime();
            PartyListVo listVo = new PartyListVo();
            BeanUtils.copyProperties(partyBo, listVo);
            if (partyBo.getStatus() != -1) {
                int status = getPartyStatus(startTimes, partyBo.getAppointment());
                if (status != partyBo.getStatus()){
                   listVo.setStatus(status);
                   updatePartyStatus(partyBo.getId(), status);
                }
            }
            listVo.setPartyid(partyBo.getId());
            listVo.setUserNum(partyBo.getPartyUserNum());
            partyListVos.add(listVo);
        }
    }

    /**
     * 获取聚会状态
     * @param startTimes
     * @param appointment
     * @return  1 进行中， 2报名结束， -1活动结束
     */
    private int getPartyStatus(LinkedHashSet<String> startTimes, int appointment){
        if (!CommonUtil.isEmpty(startTimes)) {
            Date currentZeroTime = CommonUtil.getZeroDate(new Date());
            Iterator<String> iterator = startTimes.iterator();
            String lastTime = "";
            while (iterator.hasNext()){
                lastTime = iterator.next();
            }
            if (lastTime.equals("0")) {
               return 1;
            }
            Date lastDate = CommonUtil.getDate(lastTime, "yyyy-MM-dd HH:mm");
            if (lastDate != null) {
                long last = lastDate.getTime();
                if (last <= currentZeroTime.getTime()) {
                    return -1;
                }

                //减去提前预约天数
                if (appointment > 0) {
                    last = last - (appointment * dayTimeMins);
                }
                //活动已经结束
                if (last <= System.currentTimeMillis()) {
                    return 2;
                }
            }
        }
        return 1;
    }

    @Async
    private void updatePartyStatus(String partyid, int status){
        partyService.updatePartyStatus(partyid, status);
        //聚会结束,删除所有临时聊天
        if (status == -1) {
            chatroomService.deleteTempChat(partyid, Constant.ROOM_SINGLE);
        }
    }

    /**
     * 发起群聊
     * @param partyid
     * @return
     */
    @RequestMapping("/launch-talk")
    @ResponseBody
    public String launchTalk(@RequestParam String partyid,
                            HttpServletRequest request, HttpServletResponse response){
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        PartyBo partyBo = partyService.findById(partyid);
        if (partyBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_NULL.getIndex(),
                    ERRORCODE.PARTY_NULL.getReason());
        }
        if(!partyBo.getCircleid().equals(userBo.getId())) {
            return CommonUtil.toErrorResult(ERRORCODE.CIRCLE_NOT_MASTER.getIndex(),
                    ERRORCODE.CIRCLE_NOT_MASTER.getReason());
        }
        if(StringUtils.isNotEmpty(partyBo.getChatroomid())) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_TALK_EXIST.getIndex(),
                    ERRORCODE.PARTY_TALK_EXIST.getReason());
        }

        ChatroomBo chatroomBo = new ChatroomBo();
        chatroomBo.setName(partyBo.getTitle());
        chatroomBo.setMaster(userBo.getId());
        chatroomBo.setCreateuid(userBo.getId());
        chatroomBo.setType(Constant.ROOM_MULIT);
        chatroomBo.setOpen(true);
        chatroomBo.setVerify(false);
        chatroomService.insert(chatroomBo);

        String chatroomid = chatroomBo.getId();
        
        String[] useridArr = (String[]) partyBo.getUsers().toArray();
        //第一个为返回结果信息，第二位term信息
        String result = IMUtil.subscribe(0, chatroomid, useridArr);
        if (!result.equals(IMUtil.FINISH)) {
            chatroomService.remove(chatroomid);
            return result;
        }
        userService.updateChatrooms(userBo);
        updateUserChatroom(chatroomBo, useridArr);
        partyService.updateChatroom(partyid, chatroomid);
        return Constant.COM_RESP;
    }

    /**
     * 更新用户聊天室列表
     */
    @Async
    private void updateUserChatroom(ChatroomBo chatroomBo, String[] useridArr){

        LinkedHashSet<String> users = chatroomBo.getUsers();
        for (String userid : useridArr) {
            UserBo user = userService.getUser(userid);
            if (null == user) {
                continue;
            }
            HashSet<String> chatroom = user.getChatrooms();
            //个人聊天室中没有当前聊天室，则添加到个人的聊天室
            if (!chatroom.contains(chatroomBo.getId())) {
                chatroom.add(chatroomBo.getId());
                user.setChatrooms(chatroom);
                userService.updateChatrooms(user);
            }
            users.add(userid);
            addChatroomUser(chatroomService, user, chatroomBo.getId(), user.getUserName());
        }
        chatroomBo.setUsers(users);
        chatroomService.updateUsers(chatroomBo);

    }


    /**
     * 改聚会是否已经发起了群聊
     * @param partyid
     * @return
     */
    @RequestMapping("/has-chatroom")
    @ResponseBody
    public String hasTalk(@RequestParam String partyid,
                             HttpServletRequest request, HttpServletResponse response){

        PartyBo partyBo = partyService.findById(partyid);
        if (partyBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_NULL.getIndex(),
                    ERRORCODE.PARTY_NULL.getReason());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("chatroomid", partyBo.getChatroomid());
        return JSONObject.fromObject(map).toString();
    }


    /**
     * 收藏聚会
     * @param partyid
     * @return
     */
    @RequestMapping("/collect-party")
    @ResponseBody
    public String collectParty(@RequestParam String partyid,
                             HttpServletRequest request, HttpServletResponse response){
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        PartyBo partyBo = partyService.findById(partyid);
        if (partyBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_NULL.getIndex(),
                    ERRORCODE.PARTY_NULL.getReason());
        }
        CollectBo collectBo = collectService.findByUseridAndTargetid(userBo.getId(), partyid);
        if (collectBo == null) {
            collectBo = new CollectBo();
            collectBo.setUserid(userBo.getId());
            collectBo.setTitle(partyBo.getTitle());
            collectBo.setType(Constant.COLLET_URL);
            collectBo.setSub_type(Constant.PARTY_TYPE);
            collectBo.setTargetid(partyid);
            collectService.insert(collectBo);
        } else {
            if(collectBo.getDeleted() == 1) {
                collectService.updateCollectDelete(collectBo.getId(), Constant.ACTIVITY);
            }
        }
        PartyUserBo partyUserBo = partyService.findPartyUserIgnoreDel(partyid, userBo.getId());
        if (partyUserBo == null) {
            partyUserBo = new PartyUserBo();
            partyUserBo.setUserid(userBo.getId());
            partyUserBo.setPartyid(partyid);
            partyUserBo.setStatus(0);
            partyUserBo.setCollectParty(1);
            partyService.addParty(partyUserBo);
        } else {
            partyService.collectParty(partyid, userBo.getId(), true);
        }
        updatePartyCollectNum(partyid, 1);
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("col-time", CommonUtil.time2str(collectBo.getCreateTime()));
        return JSONObject.fromObject(map).toString();
    }

    /**
     * 取消收藏
     * @param partyid
     * @return
     */
    @RequestMapping("/del-collect")
    @ResponseBody
    public String deleteCollectParty(@RequestParam String partyid,
                                     HttpServletRequest request, HttpServletResponse response){
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        PartyBo partyBo = partyService.findById(partyid);
        if (partyBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_NULL.getIndex(),
                    ERRORCODE.PARTY_NULL.getReason());
        }
        CollectBo collectBo = collectService.findByUseridAndTargetid(userBo.getId(), partyid);
        if (collectBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.COLLECT_IS_NULL.getIndex(),
                    ERRORCODE.COLLECT_IS_NULL.getReason());
        } else {
            if(collectBo.getDeleted() == 0) {
                collectService.updateCollectDelete(collectBo.getId(), Constant.DELETED);
                updatePartyCollectNum(partyid, -1);
            }
            partyService.collectParty(partyid, userBo.getId(), false);
        }
        return Constant.COM_RESP;
    }

    /**
     * 删除聚会
     * @param partyid
     * @return
     */
    @RequestMapping("/delete-party")
    @ResponseBody
    public String delelteParty(@RequestParam String partyid,
                               HttpServletRequest request, HttpServletResponse response){
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        PartyBo partyBo = partyService.findById(partyid);
        if (partyBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_NULL.getIndex(),
                    ERRORCODE.PARTY_NULL.getReason());
        }
        CircleBo circleBo = circleService.selectById(partyBo.getCircleid());
        if (!userBo.getId().equals(partyBo.getCreateuid()) &&
                !circleBo.getMasters().contains(userBo.getId())) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_NO_AUTH.getIndex(),
                    ERRORCODE.PARTY_NO_AUTH.getReason());
        }
        partyService.delete(partyid);
        partyService.deleteMulitByaPartyid(partyBo.getId());
        return Constant.COM_RESP;
    }

    /**
     * 添加评论
     * @param partyComment
     * @return
     */
    @RequestMapping("/add-comment")
    @ResponseBody
    public String addComment(String partyComment,MultipartFile[] photos,
                             HttpServletRequest request, HttpServletResponse response){
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }

        PartyComment comment = null;
        try {
            JSONObject jsonObject = JSONObject.fromObject(partyComment);
            comment = (PartyComment)JSONObject.toBean(jsonObject, PartyComment.class);
        } catch (Exception e) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_ERROR.getIndex(),
                    ERRORCODE.PARTY_ERROR.getReason());
        }

        PartyBo partyBo = partyService.findById(comment.getPartyid());
        if (partyBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_NULL.getIndex(),
                    ERRORCODE.PARTY_NULL.getReason());
        }

        List<CommentBo> commentBos = commentService.selectByTargetUser(comment.getPartyid(), userBo.getId(), Constant.PARTY_TYPE);
        if (commentBos != null && !commentBos.isEmpty()) {
            boolean hasComment = false;
            for (CommentBo commentBo : commentBos) {
                if (StringUtils.isEmpty(commentBo.getParentid())){
                    hasComment = true;
                    break;
                }
            }
            if (hasComment && StringUtils.isEmpty(comment.getParentid())) {
                return CommonUtil.toErrorResult(ERRORCODE.PARTY_HAS_COMMENT.getIndex(),
                        ERRORCODE.PARTY_HAS_COMMENT.getReason());
            }
        }

        CommentBo commentBo = new CommentBo();
        commentBo.setCreateuid(userBo.getId());
        commentBo.setContent(comment.getContent());
        commentBo.setType(Constant.PARTY_TYPE);
        commentBo.setTargetid(comment.getPartyid());
        commentBo.setUserName(userBo.getUserName());
        commentBo.setParentid(comment.getParentid());

        String userId = userBo.getId();
        Long time = Calendar.getInstance().getTimeInMillis();
        if (photos != null) {
            LinkedHashSet<String> photo = new LinkedHashSet<>();
            for (MultipartFile file : photos) {
                String fileName = userId + "-" + time + "-" + file.getOriginalFilename();
                String path = CommonUtil.upload(file, Constant.PARTY_PICTURE_PATH,
                        fileName, 0);
                photo.add(path);
            }
            commentBo.setPhotos(photo);
        }
        commentService.insert(commentBo);

        String path = "/party/party-info.do?partyid=" + comment.getPartyid();
        JPushUtil.pushMessage(titlePush, "有人刚刚评论了你的聚会，快去看看吧!", path,  partyBo.getCreateuid());
        if (!org.springframework.util.StringUtils.isEmpty(comment.getParentid())) {
            CommentBo commentBo1 = commentService.findById(comment.getParentid());
            if (commentBo1 != null) {
                JPushUtil.pushMessage(titlePush, "有人刚刚回复了你的评论，快去看看吧!", path,  commentBo1.getCreateuid());
            }
        }
        //圈子热度
        updateCircleHot(circleService, redisServer, partyBo.getCircleid(), 1, Constant.CIRCLE_PARTY_VISIT);
        if (comment.isSync()) {
            //动态信息表
            addDynamicMsgs(userId, partyBo.getId(), Constant.PARTY_COM_TYPE, dynamicService);
            updateDynamicNums(userId, 1, dynamicService, redisServer);
        }
        updateRedStar(userBo, partyBo, new Date());
        return Constant.COM_RESP;
    }

    /**
     * 更新红人信息
     * @param userBo
     * @param partyBo
     * @param currentDate
     */
    @Async
    private void updateRedStar(UserBo userBo, PartyBo partyBo, Date currentDate){
        String circleid = partyBo.getCircleid();
        RedstarBo redstarBo = commentService.findRedstarBo(userBo.getId(), circleid);
        int curretWeekNo = CommonUtil.getWeekOfYear(currentDate);
        int year = CommonUtil.getYear(currentDate);
        if (redstarBo == null) {
            redstarBo = new RedstarBo();
            redstarBo.setUserid(userBo.getId());
            redstarBo.setCommentTotal((long) 1);
            redstarBo.setCommentWeek((long) 1);
            redstarBo.setWeekNo(curretWeekNo);
            redstarBo.setCircleid(circleid);
            redstarBo.setYear(year);
            commentService.insertRedstar(redstarBo);
        }
        boolean isNotSelf = !userBo.getId().equals(partyBo.getCreateuid());
        boolean isNoteUserCurrWeek = true;
        //如果帖子作者不是自己
        if (isNotSelf) {
            //帖子作者没有红人数据信息，则添加
            RedstarBo noteRedstarBo = commentService.findRedstarBo(partyBo.getCreateuid(), circleid);
            if (noteRedstarBo == null) {
                redstarBo = new RedstarBo();
                redstarBo.setUserid(userBo.getId());
                redstarBo.setCommentTotal((long) 1);
                redstarBo.setCommentWeek((long) 1);
                redstarBo.setWeekNo(curretWeekNo);
                redstarBo.setCircleid(partyBo.getCircleid());
                redstarBo.setYear(year);
                commentService.insertRedstar(noteRedstarBo);
            } else {
                //判断帖子作者周榜是不是当前周，是则添加数据，不是则更新周榜数据
                isNoteUserCurrWeek = (year == noteRedstarBo.getYear() && curretWeekNo == noteRedstarBo.getWeekNo());
            }
        }
        //判断自己周榜是不是同一周，是则添加数据，不是则更新周榜数据
        boolean isCurrentWeek = (year == redstarBo.getYear() && curretWeekNo == redstarBo.getWeekNo());
        //更新自己或他人红人评论数量，需要加锁，保证数据准确
        RLock lock = redisServer.getRLock(Constant.COMOMENT_LOCK);
        try {
            lock.lock(5, TimeUnit.SECONDS);
            //更新自己的红人信息
            if (isCurrentWeek) {
                commentService.addRadstarCount(userBo.getId(), circleid);
            } else {
                commentService.updateRedWeekByUser(userBo.getId(), curretWeekNo, year);
            }
            if (isNotSelf) {
                //更新聚会作者的红人信息
                if (isNoteUserCurrWeek) {
                    commentService.addRadstarCount(partyBo.getCreateuid(), circleid);
                } else {
                    commentService.updateRedWeekByUser(partyBo.getCreateuid(), curretWeekNo, year);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取评论
     * @param partyid
     * @return
     */
    @RequestMapping("/get-comments")
    @ResponseBody
    public String getComment(String partyid,
                             HttpServletRequest request, HttpServletResponse response){

        PartyBo partyBo = partyService.findById(partyid);
        if (partyBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_NULL.getIndex(),
                    ERRORCODE.PARTY_NULL.getReason());
        }

        UserBo user = getUserLogin(request);

        List<CommentBo> commentBos = commentService.selectByTargetUser(partyid,"", Constant.PARTY_TYPE);

        List<CommentVo> commentVos = new ArrayList<>();
        for (CommentBo commentBo : commentBos) {
            CommentVo vo = new CommentVo();
            UserBo userBo = userService.getUser(commentBo.getCreateuid());
            if (userBo != null) {
                vo.setUserSex(userBo.getSex());
                vo.setUserHeadPic(userBo.getHeadPictureName());
                vo.setUserLevel(userBo.getLevel());
                vo.setUserName(userBo.getUserName());
                vo.setUserBirth(userBo.getBirthDay());
                vo.setUserid(userBo.getId());
            }
            if (!StringUtils.isEmpty(commentBo.getParentid())) {
                CommentBo parent = commentService.findById(commentBo.getParentid());
                vo.setParentUserName(parent.getUserName());
                vo.setParentUserid(parent.getCreateuid());
            }
            vo.setContent(commentBo.getContent());
            vo.setCommentId(commentBo.getId());
            vo.setCreateTime(commentBo.getCreateTime());
            vo.setPhotos(commentBo.getPhotos());
            if (null != user) {
                //判断当前用户是否点赞
                ThumbsupBo thumbsupBo = thumbsupService.getByVidAndVisitorid(commentBo.getId(), user.getId());
                vo.setMyThumbsup(thumbsupBo != null);
            }
            vo.setThumpsubCount(commentBo.getThumpsubNum());
            commentVos.add(vo);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("commentVos", commentVos);
        return JSONObject.fromObject(map).toString();
    }


    /**
     * 获取圈子所有聚会
     * @return
     */
    @RequestMapping("/all-partys")
    @ResponseBody
    public String getAllPartys(String circleid, int page, int limit, HttpServletRequest request, HttpServletResponse
            response){
        List<PartyBo> partyBos = partyService.findByCircleid(circleid, page, limit);
        List<PartyListVo> partyListVos = new ArrayList<>();
        bo2listVo(partyBos, partyListVos);
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("partyListVos", partyListVos);
        return JSONObject.fromObject(map).toString();
    }


    /**
     * 取消报名
     * @return
     */
    @RequestMapping("/cancel-enroll")
    @ResponseBody
    public String cancelPartys(String partyid,HttpServletRequest request, HttpServletResponse
            response){
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        RLock lock = redisServer.getRLock(partyid + "partyUserLock");
        PartyBo partyBo = null;
        try {
            lock.lock(3, TimeUnit.SECONDS);
            partyBo = partyService.findById(partyid);
            if (partyBo == null) {
                return CommonUtil.toErrorResult(ERRORCODE.PARTY_NULL.getIndex(),
                        ERRORCODE.PARTY_NULL.getReason());
            }
            LinkedList<String> users = partyBo.getUsers();
            if (users.contains(userBo.getId())) {
                users.remove(userBo.getId());
                PartyUserBo partyUserBo = partyService.findPartyUser(partyid, userBo.getId());
                int userTotal = partyBo.getPartyUserNum();
                if (partyUserBo != null) {
                    userTotal = userTotal - partyUserBo.getUserNum();
                } else {
                    userTotal--;
                }
                userTotal = userTotal < 0 ? 0 : userTotal;
                partyService.updateUser(partyid, users, userTotal);
            }
        } finally {
            lock.unlock();
        }
        partyService.outParty(partyBo.getId(), userBo.getId());
        return Constant.COM_RESP;
    }


    /**
     * 删除我参与的的聚会
     * @return
     */
    @RequestMapping("/delete-join-party")
    @ResponseBody
    public String delMyJoinPartys(String partyid, HttpServletRequest request, HttpServletResponse
            response){
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        PartyBo partyBo = partyService.findById(partyid);
        if (partyBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_NULL.getIndex(),
                    ERRORCODE.PARTY_NULL.getReason());
        }
        partyService.outParty(partyid, userBo.getId());
        return Constant.COM_RESP;
    }

    /**
     * 用户聚会报名详情
     * @return
     */
    @RequestMapping("/enroll-detail")
    @ResponseBody
    public String enrollDetail(String partyid, String userid, HttpServletRequest request, HttpServletResponse
            response){
        PartyBo partyBo = partyService.findById(partyid);
        if (partyBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_NULL.getIndex(),
                    ERRORCODE.PARTY_NULL.getReason());
        }
        PartyUserBo partyUserBo = partyService.findPartyUser(partyid, userid);
        if (partyUserBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_USER_NULL.getIndex(),
                    ERRORCODE.PARTY_USER_NULL.getReason());
        }
        PartyUserDetail userDetail = new PartyUserDetail();
        BeanUtils.copyProperties(partyUserBo, userDetail);
        UserBo userBo = userService.getUser(partyUserBo.getUserid());
        if (userBo != null) {
            userDetail.setUsername(userBo.getUserName());
            userDetail.setUserPic(userBo.getHeadPictureName());
        }
        userDetail.setAddrType(partyBo.getAddrType());
        userDetail.setAddrInfo(partyBo.getAddrInfo());
        userDetail.setStartTime(partyBo.getStartTime());
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("partyUser", userDetail);
        return JSONObject.fromObject(map).toString();
    }


    /**
     * 评论点赞
     * @return
     */
    @RequestMapping("/comment-thumbsup")
    @ResponseBody
    public String commentThumbsup(String commentId, int type, HttpServletRequest request, HttpServletResponse
            response) {
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        ThumbsupBo thumbsupBo = thumbsupService.findHaveOwenidAndVisitorid(commentId, userBo.getId());
        int num = 0;
        if (type == 0) {
            if (null == thumbsupBo) {
                thumbsupBo = new ThumbsupBo();
                thumbsupBo.setType(Constant.PARTY_COM_TYPE);
                thumbsupBo.setOwner_id(commentId);
                thumbsupBo.setImage(userBo.getHeadPictureName());
                thumbsupBo.setVisitor_id(userBo.getId());
                thumbsupBo.setCreateuid(userBo.getId());
                thumbsupService.insert(thumbsupBo);
            } else {
                if (thumbsupBo.getDeleted() == Constant.DELETED) {
                    thumbsupService.udateDeleteById(thumbsupBo.getId());
                }
            }
            num = 1;
        } else if (type == 1) {
            if (null != thumbsupBo && thumbsupBo.getDeleted() == Constant.ACTIVITY) {
                thumbsupService.deleteById(thumbsupBo.getId());
                num = -1;
            }
        } else {
            return CommonUtil.toErrorResult(ERRORCODE.TYPE_ERROR.getIndex(),
                    ERRORCODE.TYPE_ERROR.getReason());
        }
        RLock lock = redisServer.getRLock(commentId);
        try {
            lock.lock(1, TimeUnit.SECONDS);
            commentService.updateThumpsubNum(commentId, num);
        } finally {
            lock.unlock();
        }
        if (type == 1){
            CommentBo commentBo = commentService.findById(commentId);
            if (commentBo != null) {
                String path = "/party/party-info.do?partyid=" + commentBo.getTargetid();
                JPushUtil.pushMessage(titlePush, "有人刚刚赞了你的聚会，快去看看吧!", path,  commentBo.getCreateuid());
            }
        }
        return Constant.COM_RESP;
    }


    /**
     * 举报聚会
     * @param partyid
     * @return
     */
    @RequestMapping("/tip-off-party")
    @ResponseBody
    public String tipOffParty(@RequestParam String partyid,
                               HttpServletRequest request, HttpServletResponse response){
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        PartyBo partyBo = partyService.findById(partyid);
        if (partyBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_NULL.getIndex(),
                    ERRORCODE.PARTY_NULL.getReason());
        }

        return "";
    }

    @Async
    private void updatePartyCollectNum(String partyid, int num){
        RLock lock = redisServer.getRLock(partyid + "partyCollect");
        try {
            lock.lock(2, TimeUnit.SECONDS);
            partyService.updateCollect(partyid, num);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 复制参数 ,将old参数都复制到new上，如果new不存在，则复制
     * @param oldParty
     * @param newParty
     */
    private void copyOld(PartyBo oldParty, PartyBo newParty) {
        // 获取属性
        try {
            BeanInfo sourceBean = Introspector.getBeanInfo(oldParty.getClass(), java.lang.Object.class);
            PropertyDescriptor[] sourceProperty = sourceBean.getPropertyDescriptors();
            BeanInfo destBean = Introspector.getBeanInfo(newParty.getClass(), java.lang.Object.class);
            PropertyDescriptor[] destProperty = destBean.getPropertyDescriptors();
            for (int i = 0; i < sourceProperty.length; i++) {
                for (int j = 0; j < destProperty.length; j++) {
                    PropertyDescriptor descriptors = destProperty[j];
                    if (sourceProperty[i].getName().equals(descriptors.getName())) {
                        Object value = descriptors.getReadMethod().invoke(newParty);
                        //如果new的为空 或者
                        if (value == null) {
                            descriptors.getWriteMethod().invoke(newParty, sourceProperty[i].getReadMethod().invoke(oldParty));
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @RequestMapping("/temp-chatroom")
    @ResponseBody
    public String tempChatroom(String partyid, String friendid,
                               HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        UserBo friend = userService.getUser(friendid);
        if (friend == null) {
            return CommonUtil.toErrorResult(ERRORCODE.USER_NULL.getIndex(),
                    ERRORCODE.USER_NULL.getReason());
        }
        PartyBo partyBo = partyService.findById(partyid);
        if (partyBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_HAS_END.getIndex(),
                    ERRORCODE.PARTY_HAS_END.getReason());
        }

        String userid = userBo.getId();
        ChatroomBo chatroomBo = chatroomService.selectByUserIdAndFriendid(userid, friendid);
        if (chatroomBo == null) {
            chatroomBo = chatroomService.selectByUserIdAndFriendid(friendid, userid);
        }
        //单人聊天也存在免打扰信息
        if (chatroomBo == null) {
            chatroomBo = new ChatroomBo();
            chatroomBo.setName(friend.getUserName());
            chatroomBo.setUserid(userid);
            chatroomBo.setFriendid(friendid);
            chatroomBo.setType(Constant.ROOM_SINGLE);
            chatroomBo.setTargetid(partyid);
            chatroomBo.setCreateuid(userid);
            chatroomService.insert(chatroomBo);
            String res = IMUtil.subscribe(0,chatroomBo.getId(), userid, friendid);
            if (!res.equals(IMUtil.FINISH)) {
                return res;
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("channelId", chatroomBo.getId());
        return JSONObject.fromObject(map).toString();
    }

    /**
     * 发送通知
     * @param partyid
     * @param content
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/send-notice")
    @ResponseBody
    public String sendNotice(String partyid, String content,
                               HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        PartyBo partyBo = partyService.findById(partyid);
        if (partyBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_HAS_END.getIndex(), ERRORCODE.PARTY_HAS_END.getReason());
        }
        if (!partyBo.getCreateuid().equals(userBo.getId())) {
            return CommonUtil.toErrorResult(ERRORCODE.NOTE_NOT_MASTER.getIndex(), ERRORCODE.NOTE_NOT_MASTER.getReason());
        }
        LinkedList users = partyBo.getUsers();
        PartyNoticeBo noticeBo = new PartyNoticeBo();
        noticeBo.setPartyid(partyid);
        noticeBo.setContent(content);
        noticeBo.setUsers(users);
        noticeBo.setCreateuid(userBo.getId());
        partyService.addPartyNotice(noticeBo);

        if (users.size() > 0) {
            String path = "/party/notice.do?noticeid=" +noticeBo.getId();
            String[] userids = new String[users.size()];
            users.toArray(userids);
            JPushUtil.push(titlePush, content, path, userids);
        }
        return Constant.COM_RESP;
    }

    /**
     * 通知详情
     * @param noticeid
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/notice")
    @ResponseBody
    public String getNotice(String noticeid,
            HttpServletRequest request, HttpServletResponse response) {

        PartyNoticeBo partyNoticeBo = partyService.findNoticeById(noticeid);
        if (partyNoticeBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_NOTICE_NULL.getIndex(),
                    ERRORCODE.PARTY_NOTICE_NULL.getReason());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("noticeVo", partyNoticeBo);
        return JSONObject.fromObject(map).toString();
    }

    /**
     * 聚会的所有通知
     * @param partyid
     * @param page
     * @param limit
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/party-notice")
    @ResponseBody
    public String partyNotice(String partyid, int page, int limit,
                            HttpServletRequest request, HttpServletResponse response) {
        PartyBo partyBo = partyService.findById(partyid);
        if (partyBo == null) {
            return CommonUtil.toErrorResult(ERRORCODE.PARTY_HAS_END.getIndex(), ERRORCODE.PARTY_HAS_END.getReason());
        }
        List<PartyNoticeBo> noticeBos = partyService.findNoticeByPartyid(partyid, page, limit);
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("noticeVos", noticeBos);
        return JSONObject.fromObject(map).toString();
    }

    /**
     * 我收到的通知
     * @param page
     * @param limit
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/get-my-notices")
    @ResponseBody
    public String getPartyNotices(int page, int limit,
                              HttpServletRequest request, HttpServletResponse response) {
        UserBo userBo;
        try {
            userBo = checkSession(request, userService);
        } catch (MyException e) {
            return e.getMessage();
        }
        List<PartyNoticeBo> noticeBos = partyService.findNoticeByPartyid(userBo.getId(), page, limit);
        Map<String, Object> map = new HashMap<>();
        map.put("ret", 0);
        map.put("noticeVos", noticeBos);
        return JSONObject.fromObject(map).toString();
    }

}
