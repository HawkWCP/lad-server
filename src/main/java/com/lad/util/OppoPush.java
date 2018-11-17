package com.lad.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oppo.push.server.Notification;
import com.oppo.push.server.Result;
import com.oppo.push.server.Sender;
import com.oppo.push.server.Target;

public class OppoPush {
	private static final String appKey = "b64b631b5f6b4699bb990ace27d797f1";
	private static final String masterSecret = "8d32b707ff46463d9685fcd59897ed1e";

	/**
	 * 单推
	 * 
	 * @param pushTitle
	 * @param title
	 * @param content
	 * @param targetId
	 */
	public static void send2One(String pushTitle, String title, String content, String targetId) {
		try {
			Sender sender = new Sender(appKey, masterSecret);

			Notification notification = getNotification(pushTitle, title, content); // 创建通知栏消息体

			Target target = Target.build("CN_1f32767bd4e0c86aa97aa37f915f28f2"); // 创建发送对象
			Result result = sender.unicastNotification(notification, target); // 发送单推消息

			result.getStatusCode(); // 获取http请求状态码

			result.getReturnCode(); // 获取平台返回码

			result.getMessageId(); // 获取平台返回的messageId
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 群推
	 * 
	 */
	public static void send2Many(String pushTitle, String title, String content, List<String> targetIds) {
		try {
			Sender sender = new Sender(appKey, masterSecret);

			Map<Target, Notification> batch = new HashMap<Target, Notification>(); // batch最大为1000
			Notification notification = getNotification(pushTitle, title, content);
			for (String targetId : targetIds) {
				batch.put(Target.build(targetId), notification);
			}

			Result result = sender.unicastBatchNotification(batch); // 发送批量单推消息

			result.getStatusCode(); // 获取http请求状态码

			result.getReturnCode(); // 获取平台返回码

			List<Result.UnicastBatchResult> batchResult = result.getUnicastBatchResults(); // 批量单推结果

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 广播通知
	 * 
	 * @throws Exception
	 */
	public static void broadNotification(String pushTitle, String title, String content, List<String> targetIds) {
		try {		Sender sender = new Sender(appKey, masterSecret);

		Notification broadNotification = getNotification(pushTitle, title, content);// 创建通知栏消息体

		Result saveResult = sender.saveNotification(broadNotification); // 发送保存消息体请求

		saveResult.getStatusCode(); // 获取http请求状态码

		saveResult.getReturnCode(); // 获取平台返回码

		String messageId = saveResult.getMessageId(); // 获取messageId

		Target target = new Target(); // 创建广播目标
		String targetValues = "";

		for (String targetStr : targetIds) {
			targetStr += targetStr + ";";
		}
		target.setTargetValue(targetValues.substring(targetValues.length() - 1, targetValues.length()));

		Result broadResult = sender.broadcastNotification(messageId, target); // 发送广播消息

		broadResult.getTaskId(); // 获取广播taskId

		List<Result.BroadcastErrorResult> errorList = broadResult.getBroadcastErrorResults();

		if (errorList.size() > 0) { // 如果大小为0，代表所有目标发送成功

			for (Result.BroadcastErrorResult error : errorList) {

				error.getErrorCode(); // 错误码

				error.getTargetValue(); // 目标
			}
		}}catch(Exception e) {e.printStackTrace();}

	}

//	创建通知栏消息体

	private static Notification getNotification(String pushTitle, String title, String content) {

		Notification notification = new Notification();

		/* 必填项 */
		notification.setTitle(pushTitle);

		notification.setSubTitle(title);

		notification.setContent(content);

		/* 选填项 */
		notification.setClickActionType(4);
		notification.setClickActionActivity("com.coloros.push.demo.component.InternalActivity");
		notification.setShowTimeType(0);
		notification.setOffLineTtl(24 * 3600 * 3);

		/**
		 * 
		 * 以下参数非必填项， 如果需要使用可以参考OPPO push服务端api文档进行设置 //
		 * App开发者自定义消息Id，OPPO推送平台根据此ID做去重处理，对于广播推送相同appMessageId只会保存一次，对于单推相同appMessageId只会推送一次
		 * 
		 * notification.setAppMessageId(UUID.randomUUID().toString());
		 * 
		 * // 应用接收消息到达回执的回调URL，字数限制200以内，中英文均以一个计算
		 * 
		 * notification.setCallBackUrl("http://www.test.com");
		 * 
		 * // App开发者自定义回执参数，字数限制50以内，中英文均以一个计算
		 * 
		 * notification.setCallBackParameter("");
		 * 
		 * // 点击动作类型0，启动应用；1，打开应用内页（activity的intent
		 * action）；2，打开网页；4，打开应用内页（activity）；【非必填，默认值为0】;5,Intent scheme URL
		 * 
		 * notification.setClickActionType(4);
		 * 
		 * // 应用内页地址【click_action_type为1或4时必填，长度500】
		 * 
		 * notification.setClickActionActivity("com.coloros.push.demo.component.InternalActivity");
		 * 
		 * // 网页地址【click_action_type为2必填，长度500】
		 * 
		 * notification.setClickActionUrl("http://www.test.com");
		 * 
		 * //
		 * 动作参数，打开应用内页或网页时传递给应用或网页【JSON格式，非必填】，字符数不能超过4K，示例：{"key1":"value1","key2":"value2"}
		 * 
		 * notification.setActionParameters("{\"key1\":\"value1\",\"key2\":\"value2\"}");
		 * 
		 * // 展示类型 (0, “即时”),(1, “定时”)
		 * 
		 * notification.setShowTimeType(1);
		 * 
		 * // 定时展示开始时间（根据time_zone转换成当地时间），时间的毫秒数
		 * 
		 * notification.setShowStartTime(System.currentTimeMillis() + 1000 * 60 * 3);
		 * 
		 * // 定时展示结束时间（根据time_zone转换成当地时间），时间的毫秒数
		 * 
		 * notification.setShowEndTime(System.currentTimeMillis() + 1000 * 60 * 5);
		 * 
		 * // 是否进离线消息,【非必填，默认为True】
		 * 
		 * notification.setOffLine(true);
		 * 
		 * // 离线消息的存活时间(time_to_live) (单位：秒), 【off_line值为true时，必填，最长3天】
		 * 
		 * notification.setOffLineTtl(24 * 3600);
		 * 
		 * // 时区，默认值：（GMT+08:00）北京，香港，新加坡
		 * 
		 * notification.setTimeZone("GMT+08:00");
		 * 
		 * // 0：不限联网方式, 1：仅wifi推送
		 * 
		 * notification.setNetworkType(0);
		 */

		return notification;

	}
}
