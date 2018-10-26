package com.lad.init;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lad.util.CommonUtil;

public class SensitiveReponseWrapper extends HttpServletResponseWrapper {

	private Logger logger = LogManager.getLogger();

	private ByteArrayOutputStream buffer;

	private ServletOutputStream out;

	public SensitiveReponseWrapper(HttpServletResponse httpServletResponse) {
		super(httpServletResponse);
		buffer = new ByteArrayOutputStream();
		out = new WrapperOutputStream(buffer);
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return out;
	}

	@Override
	public void flushBuffer() throws IOException {
		if (out != null) {
			out.flush();
		}
	}

	public byte[] getContent() throws IOException {
		flushBuffer();
		return sensitiveQuery(buffer.toString()).getBytes();
	}

	class WrapperOutputStream extends ServletOutputStream {
		private ByteArrayOutputStream bos;

		public WrapperOutputStream(ByteArrayOutputStream bos) {
			this.bos = bos;
		}

		@Override
		public void write(int b) throws IOException {
			bos.write(b);
		}

		@Override
		public boolean isReady() {

			return false;

		}

		@Override
		public void setWriteListener(WriteListener writeListener) {

		}
	}

	private String sensitiveQuery(String str) {
		String url = "http://wf.ttlaoyou.com/v1/query";
//		String url = "http://localhost:8090/v1/query";

		// 过滤响应
		JSONObject response = JSON.parseObject(str);
		int ret = (int) response.get("ret");
		if (ret != 0) {
			return str;
		}
		try {
			// 发送响应语句进行检查,并返回结果keywords
			Map<String, String> params = new HashMap<>();
			params.put("q", str);
			String sendPost = sendPost(url, params);
			JSONObject object = JSON.parseObject(sendPost);
			JSONObject keywords = object.getJSONObject("data").getJSONObject("keywords");

			if (!keywords.isEmpty()) {
				response.put("havaSensitiveWord", true);

				List<SensiwordWrapper> sensitiveAnalysis = new ArrayList<>();
				for (Entry<String, Object> entry : keywords.entrySet()) {
					SensiwordWrapper sensiwordWrapper = new SensiwordWrapper();
					String sensiword = entry.getKey();
					sensiwordWrapper.setSensitiveWord(sensiword);

					int num = (int) entry.getValue();
					sensiwordWrapper.setNum(num);

					List<Map<String, Object>> position = new ArrayList<>();
					for (Entry<String, Object> resEntry : response.entrySet()) {
						if (resEntry.getValue().toString().contains(sensiword)) {
							Map<String, Object> map = new HashMap<>(
									CommonUtil.getIndex(resEntry.getValue().toString(), sensiword));
							map.put("field", resEntry.getKey());
							position.add(map);

							if (resEntry.getValue() instanceof java.lang.String) {
								String value = (String) resEntry.getValue();
								response.put(resEntry.getKey(), JSON.parseObject(value.replaceAll(sensiword, "**")));
							}

							if (resEntry.getValue() instanceof com.alibaba.fastjson.JSONArray) {
								com.alibaba.fastjson.JSONArray value = (com.alibaba.fastjson.JSONArray) resEntry
										.getValue();

								response.put(resEntry.getKey(),
										JSON.parseArray(value.toString().replaceAll(sensiword, "**")));

							}
						}
					}
					sensiwordWrapper.setPosition(position);
					sensitiveAnalysis.add(sensiwordWrapper);
				}
				response.put("sensitiveAnalysis", sensitiveAnalysis);
				str = response.toJSONString();
			} else {

				response.put("havaSensitiveWord", false);
				response.put("sensitiveWord", new ArrayList<>());
				str = response.toJSONString();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return str;
	}

	/*
	 * private String getSensitives(){ // String url =
	 * "http://wf.ttlaoyou.com/v1/black_words"; String url =
	 * "http://localhost:8090/v1/black_words"; String sendGet = sendGet(url);
	 * JSONObject object = JSON.parseObject(sendGet); return sendGet(url); }
	 */

	private String sendPost(String url, Map<String, String> params) {
		try {
			List<BasicNameValuePair> formparams = new ArrayList<>();
			for (Entry<String, String> entry : params.entrySet()) {
				formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}

			HttpEntity reqEntity = new UrlEncodedFormEntity(formparams, "utf-8");

			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000)// 一、连接超时：connectionTimeout-->指的是连接一个url的连接等待时间
					.setSocketTimeout(5000)// 二、读取数据超时：SocketTimeout-->指的是连接上一个url，获取response的返回等待时间
					.setConnectionRequestTimeout(5000).build();

			CloseableHttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(url);
			post.setEntity(reqEntity);
			post.setConfig(requestConfig);
			HttpResponse response = client.execute(post);

			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity resEntity = response.getEntity();
				String message = EntityUtils.toString(resEntity, "utf-8");
				return message;
			} else {
				return "请求失败";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private class SensiwordWrapper {
		private String sensitiveWord;
		private int num;
		private List<Map<String, Object>> position = new ArrayList<Map<String, Object>>();

		public String getSensitiveWord() {
			return sensitiveWord;
		}

		public void setSensitiveWord(String sensitiveWord) {
			this.sensitiveWord = sensitiveWord;
		}

		public int getNum() {
			return num;
		}

		public void setNum(int num) {
			this.num = num;
		}

		public List<Map<String, Object>> getPosition() {
			return position;
		}

		public void setPosition(List<Map<String, Object>> position) {
			this.position = position;
		}
	}
}
