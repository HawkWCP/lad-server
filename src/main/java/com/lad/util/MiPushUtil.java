package com.lad.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaomi.xmpush.server.Constants;
import com.xiaomi.xmpush.server.Feedback;
import com.xiaomi.xmpush.server.Message;
import com.xiaomi.xmpush.server.Result;
import com.xiaomi.xmpush.server.Sender;
import com.xiaomi.xmpush.server.TargetedMessage;

@SuppressWarnings("all")
public class MiPushUtil {
	private static String APP_SECRET_KEY = "fO9/I7KsEYd0vQ11VLYyPA==";
	private static String MY_PACKAGE_NAME = "com.ttlaoyou";
	private static Logger logger = LogManager.getLogger();

	/**
	 * 多个alias消息(推荐使用)
	 * 
	 * @param messagePayload
	 * @param title
	 * @param description
	 * @param aliasList
	 * @throws Exception
	 */
	public static void sendMessageToAliases(String title, String messagePayload,String description, String path,List<String> aliasList) {
		try {
			Constants.useOfficial();
			Sender sender = new Sender(APP_SECRET_KEY);
			Message message = new Message.Builder()
					.title(title)
					.description(description)
					.payload(messagePayload)
					.restrictedPackageName(MY_PACKAGE_NAME)
					.extra("path", path)
					.notifyType(1) // 使用默认提示音提示
					.build();
			Result sendToAlias = sender.sendToAlias(message, aliasList, 3);
			//{"errorCode":{"name":"成功","description":"成功","fullDescription":"成功,0,成功","value":0},"messageId":"adm04750540195694272cS","data":{"id":"adm04750540195694272cS"}}
			logger.info("mipush=====" + JSON.toJSON(sendToAlias));
		} catch (Exception e) {
			logger.error("Mipush.sendMessage throw error:{}", e.toString());
		}
	}

	/**
	 * 
	 * @param messagePayload
	 * @param title
	 * @param description
	 * @param accoutList useraccount非空白，不能包含逗号, 长度小于128
	 * @throws Exception
	 */
	public static void sendMessageToUserAccounts(String messagePayload, String title, String description,
			List<String> accoutList) {

		try {
			Constants.useOfficial();
			Sender sender = new Sender(APP_SECRET_KEY);
			Message message = new Message.Builder().title(title).description(description).payload(messagePayload)
					.restrictedPackageName(MY_PACKAGE_NAME).notifyType(1) // 使用默认提示音提示
					.build();
			sender.sendToUserAccount(message, accoutList, 3); // 根据accountList,发送消息到指定设备上
		} catch (Exception e) {
			logger.error("Mipush.sendMessage throw error:{}", e.toString());
		}

	}

	/**
	 * 使用targetMessage给不同用户发送不通消息
	 * 
	 * @param messagePayload
	 * @param messagePayload1
	 * @param title
	 * @param title1
	 * @param description
	 * @param description1
	 * @param alias
	 * @param alias1
	 * @throws Exception
	 */
	public static void sendMessageToAlias(String messagePayload, String messagePayload1, String title, String title1,
			String description, String description1, String alias, String alias1) {

		try {
			Constants.useOfficial();
			Sender sender = new Sender(APP_SECRET_KEY);

			Message message = new Message.Builder().title(title).description(description).payload(messagePayload)
					.restrictedPackageName(MY_PACKAGE_NAME).notifyType(1) // 使用默认提示音提示
					.build();

			TargetedMessage targetedMessage = new TargetedMessage();
			targetedMessage.setTarget(TargetedMessage.TARGET_TYPE_ALIAS, alias);
			targetedMessage.setMessage(message);

			TargetedMessage targetedMessage1 = new TargetedMessage();
			targetedMessage1.setTarget(TargetedMessage.TARGET_TYPE_ALIAS, alias1);
			Message message1 = new Message.Builder().title(title1).description(description1).payload(messagePayload1)
					.restrictedPackageName(MY_PACKAGE_NAME).notifyType(1) // 使用默认提示音提示
					.build();
			targetedMessage1.setMessage(message1);
			List<TargetedMessage> messages = new ArrayList<TargetedMessage>();
			messages.add(targetedMessage);
			messages.add(targetedMessage1);

			// 根据TargetedMessage列表, 发送消息到指定设备上, 设置一个小时后发送
			sender.send(messages, 3, System.currentTimeMillis() + 60 * 60 * 1000);
		} catch (Exception e) {
			logger.error("Mipush.sendMessage throw error:{}", e.toString());
		}

	}

	/**
	 * 使用targetMessage发送消息
	 * 
	 * @param messagePayload
	 * @param title
	 * @param description
	 * @param alias
	 * @throws Exception
	 */
	public static void sendTargetedMessageToAlia(String messagePayload, String title, String description,
			String alias) {

		try {
			Constants.useOfficial();
			Sender sender = new Sender(APP_SECRET_KEY);
			List<TargetedMessage> messages = new ArrayList<TargetedMessage>();

			Message message = new Message.Builder().title(title).description(description).payload(messagePayload)
					.restrictedPackageName(MY_PACKAGE_NAME).notifyType(1) // 使用默认提示音提示
					.build();
			TargetedMessage targetedMessage = new TargetedMessage();
			// targetedMessage.setTarget(TargetedMessage.TARGET_TYPE_ALIAS,
			// alias);
			targetedMessage.setMessage(message);
			messages.add(targetedMessage);

			// 根据TargetedMessage列表, 发送消息到指定设备上, 设置一个小时后发送
			sender.send(messages, 3, System.currentTimeMillis() + 60 * 60 * 1000);
		} catch (Exception e) {
			logger.error("Mipush.sendMessage throw error:{}", e.toString());
		}

	}

	private static void getInvalidRegIds() {
		Feedback feedback = new Feedback(APP_SECRET_KEY);
		String invalidRegIds = "";
		try {
			invalidRegIds = feedback.getInvalidRegIds(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JSONObject parseObject = JSON.parseObject(invalidRegIds);
		if ("ok".equals(parseObject.get("result"))) {
			logger.info(parseObject.get("data").getClass());
			logger.info(parseObject.get("data").toString());
		} else {
			logger.info("未获取到失效别名");
		}
	}
}
