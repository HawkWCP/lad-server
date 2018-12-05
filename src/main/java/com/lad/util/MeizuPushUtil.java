package com.lad.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.lad.bo.UserBo;
import com.meizu.push.sdk.server.IFlymePush;
import com.meizu.push.sdk.server.constant.ErrorCode;
import com.meizu.push.sdk.server.constant.PushResponseCode;
import com.meizu.push.sdk.server.constant.ResultPack;
import com.meizu.push.sdk.server.model.push.PushResult;
import com.meizu.push.sdk.server.model.push.VarnishedMessage;

public class MeizuPushUtil {

	private static final long appId = 116349;
	private static final String appSecret = "393f08c32d4c428eab8f3650fb1188d5";
	private static Logger logger = LogManager.getLogger();

	public static void pushMessageByAlias(String title,String content,String path,List<String> alias) {
		logger.info("魅族推送=====title:{},content:{},path:{},alias:{}", title,content,path,alias);
		try {
			IFlymePush push = new IFlymePush(appSecret);
			VarnishedMessage message = new VarnishedMessage.Builder()
					.appId(appId)
					.title(title)
					.content(content)
					.clickType(3)
					.customAttribute(path)
					.suspend(true)
					.build();
			ResultPack<PushResult> resultPack = push.pushMessageByAlias(message, alias);
			if(resultPack.isSucceed()) {
				PushResult result = resultPack.value();
				Map<String, List<String>> target = result.getRespTarget();

				if(target!=null&&!target.isEmpty()) {
					if(target.containsKey(PushResponseCode.RSP_SPEED_LIMIT.getValue())) {
						List<String> rateLimitTarget = target.get(PushResponseCode.RSP_SPEED_LIMIT.getValue());
						System.out.println("rateLimitTarget is:"+rateLimitTarget);
						// TODO
					}
				}
			}else {
				if(String.valueOf(ErrorCode.APP_REQUEST_EXCEED_LIMIT.getValue()).equals(resultPack.code())) {
						//	TODO
				}
				System.out.println(String.format("pushmessage error code:%s , comment:%s", resultPack.code(),resultPack.comment()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void pushMessageById() {
		UserBo user = new UserBo();
		user.setUserName("元角分");
		try {
			IFlymePush push = new IFlymePush(appSecret);
			VarnishedMessage message = new VarnishedMessage.Builder()
					.appId(appId)
					.title("push test title")
					.content("push test content")
					.noticeExpandType(1)
					.noticeExpandContent("noticeExpandContent:展开文本内容")
					.clickType(2)
					.url("http://www.baidu.com")
					.parameters(JSON.parseObject(JSON.toJSONString(user)))
					.offLine(true)
					.validTime(12)
					.suspend(true)
					.clearNoticeBar(true)
					.vibrate(true)
					.lights(true)
					.sound(true)
					.build();
			List<String> pushIds = new ArrayList<>();
			pushIds.add("59cfa42831f0a51d1e047420");
			ResultPack<PushResult> resultPack = push.pushMessage(message, pushIds);
			if(resultPack.isSucceed()) {
				PushResult result = resultPack.value();
				String msgId = result.getMsgId();
				System.out.println("msgId is:"+msgId);
				Map<String, List<String>> target = result.getRespTarget();
				System.out.println("  is:"+target);

				if(target!=null&&!target.isEmpty()) {
					if(target.containsKey(PushResponseCode.RSP_SPEED_LIMIT.getValue())) {
						List<String> rateLimitTarget = target.get(PushResponseCode.RSP_SPEED_LIMIT.getValue());
						System.out.println("rateLimitTarget is:"+rateLimitTarget);
						// TODO
					}
				}
			}else {
				if(String.valueOf(ErrorCode.APP_REQUEST_EXCEED_LIMIT.getValue()).equals(resultPack.code())) {
						//	TODO
				}
				System.out.println(String.format("pushmessage error code:%s , comment:%s", resultPack.code(),resultPack.comment()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
