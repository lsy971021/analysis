package com.icourt.util;

import com.alibaba.fastjson.JSON;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.util.IdleConnectionTimeoutThread;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * @author lilx
 */
@SuppressWarnings("all")
public class HttpUtils {
    private static final Log logger = LogFactory.getLog(HttpUtils.class);
	//指的是连接一个url的连接等待时间
	public static final int DEFAULT_CONNECTION_TIMEOUT = 1000*10;
	//指的是连接上一个url，获取response的返回等待时间
	public static final int DEFAULT_READ_TIMEOUT = 2000*60;

//	public static final int DEFAULT_CONNECTION_TIMEOUT = 1000*10;
//	public static final int DEFAULT_READ_TIMEOUT = 1000*60;
	public static final int MAX_TOTAL_CONNECTIONS = 500;
	public static final int DEFAULT_MAX_CONNECTIONSPERHOST = 200;
	public static final int IDLETHREAD_TIMEOUT_INTERVAL = 1000*150;
	public static final int IDLETHREAD_CONNECTION_TIMEOUT = 1000*150;
	public static final OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
			.build();
	private static final MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
	private static final HttpClient client = new HttpClient(connectionManager);
	static {
		System.setProperty("org.apache.commons.logging.Log","org.apache.commons.logging.impl.SimpleLog");
	    System.setProperty("org.apache.commons.logging.simplelog.showdatetime","true");
	    System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "error");

		client.getHttpConnectionManager().getParams().setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);
		client.getHttpConnectionManager().getParams().setSoTimeout(DEFAULT_READ_TIMEOUT);
		client.getHttpConnectionManager().getParams().setMaxTotalConnections(MAX_TOTAL_CONNECTIONS);
		client.getHttpConnectionManager().getParams().setDefaultMaxConnectionsPerHost(DEFAULT_MAX_CONNECTIONSPERHOST);

		IdleConnectionTimeoutThread idleThread = new IdleConnectionTimeoutThread();
		idleThread.setTimeoutInterval(IDLETHREAD_TIMEOUT_INTERVAL);
		idleThread.setConnectionTimeout(IDLETHREAD_CONNECTION_TIMEOUT);
		idleThread.addConnectionManager(connectionManager);
		idleThread.start();
	}
	/**
	 *
	 * @param url
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String httpPost(String url, Map<String, String> params )
			throws Exception {
		return httpPost( url,  params,false);
	}

	public static String httpPostUTF8(String url, Map<String, String> params )
			throws Exception {
		return httpPostUTF8( url,  params,false);
	}

	public static String httpPost(String url, Map<String, String> params, boolean keepLive)
			throws Exception {
		PostMethod post = new PostMethod(url);
		if(keepLive){
			post.setRequestHeader("Connection" , "Keep-Alive" ) ;
		}
		NameValuePair[] nameValuePairs = buildNameValuePairs(params);
		if (nameValuePairs != null) {
			post.setRequestBody(nameValuePairs);
		}
		try {
			client.executeMethod(post);
			if(post.getStatusCode() > 299) {
				return null;
			}
			return post.getResponseBodyAsString();
		} catch (Exception ex) {
			logger.error(url + " post request fail! "+ ex.getMessage());
			throw new Exception("", ex);
		} finally {
			post.releaseConnection();
		}
	}

	public static String httpPostUTF8(String url, Map<String, String> params,boolean keepLive)
			throws Exception {
		PostMethod post = new PostMethod(url);
		if(keepLive) {
			post.setRequestHeader("Connection" , "Keep-Alive" ) ;
		}
		//post.addRequestHeader("Content-Type","text/html;charset=UTF-8");
		NameValuePair[] nameValuePairs = buildNameValuePairs(params);
		if (nameValuePairs != null) {
			post.setRequestBody(nameValuePairs);
		}
		try {
			post.getParams().setContentCharset("utf-8");
			client.executeMethod(post);
			if(post.getStatusCode()>299){
				return null;
			}
			return post.getResponseBodyAsString();
		} catch (Exception ex) {
			logger.error(url + " post request fail! " + ex.getMessage());
			throw new Exception("", ex);
		} finally {
			post.releaseConnection();
		}
	}

	public static String httpPost(String url, Map<String, String> params, String body) {
		return httpPost(url, params, body, ContentType.APPLICATION_JSON.getMimeType(), "utf-8", DEFAULT_CONNECTION_TIMEOUT,
				DEFAULT_READ_TIMEOUT);
	}

	public static String httpPost(String url, Map<String, String> params, String body,
			String contentType, String charset, int connectionTimeout, int readTimeout) {
		PostMethod post = new PostMethod(url);
		NameValuePair[] nameValuePairs = buildNameValuePairs(params);
		if (nameValuePairs != null) {
			post.setQueryString(nameValuePairs);
		}
		try {
			post.setRequestEntity(new StringRequestEntity(body, contentType, charset));
			client.executeMethod(post);
			if(post.getStatusCode()>299){
				return null;
			}
			return post.getResponseBodyAsString();
		} catch (Exception ex) {
			logger.error(ex);
			ex.getStackTrace();
		} finally {
			post.releaseConnection();
		}
		return charset;
	}


	private static NameValuePair[] buildNameValuePairs(Map<String, String> params) {
		NameValuePair[] data = null;
		if (params != null && params.size() > 0) {
			Set<Entry<String, String>> entrySet = params.entrySet();
			data = new NameValuePair[entrySet.size()];

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			for (Entry<String, String> entry : entrySet) {
				nameValuePairs.add(new NameValuePair(entry.getKey(), entry.getValue()));
			}
			nameValuePairs.toArray(data);
		}
		return data;
	}

	private static NameValuePair[] buildNameValuePairs2(Map<String, Object> params) {
		NameValuePair[] data = null;
		if (params != null && params.size() > 0) {
			Set<Entry<String, Object>> entrySet = params.entrySet();
			data = new NameValuePair[]{};

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			for (Entry<String, Object> entry : entrySet) {
				if (entry.getValue() != null) {
					nameValuePairs.add(new NameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
				}
			}
			data = nameValuePairs.toArray(data);
		}
		return data;
	}

	public static String xmlHttpPost(String xmlRequest, String url) throws Exception{
		String resXml = "";
		//HttpClient client = new HttpClient(connectionManager);
		PostMethod method = new PostMethod(url);
		try {
			//method.setHttp11(true);
			method.setRequestHeader("Content-type", "text/xml; charset=utf-8");
			method.setRequestEntity(new StringRequestEntity(xmlRequest, "text/xml", "utf-8"));
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

			int status = client.executeMethod(method);
			if (status == HttpStatus.SC_OK) {
				try {
					resXml = method.getResponseBodyAsString();
				} catch (Exception e) {
					logger.error(url + " post request fail! " + e.getMessage());
					throw e;
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			method.releaseConnection();
			//((SimpleHttpConnectionManager)client.getHttpConnectionManager()).shutdown();

		}
		return resXml;
	}

	public static String httpGet(String url, Map<String, Object> params, Map<String, String> headers) {
		GetMethod get = new GetMethod(url);
		if (headers != null) {
			for (String key : headers.keySet()) {
				get.setRequestHeader(key, headers.get(key));
			}
		}
		NameValuePair[] nameValuePairs = buildNameValuePairs2(params);
		if (nameValuePairs != null) {
			get.setQueryString(nameValuePairs);
		}
		try {
			client.executeMethod(get);
			if(get.getStatusCode() > 299){
				return null;
			}
			return get.getResponseBodyAsString();
		} catch (Exception ex) {
			logger.error(url + " get request fail! ", ex);
		} finally {
			get.releaseConnection();
		}

		return null;
	}

	/**
	 * 获取状态码
	 *
	 * @param url 链接
	 * @return 状态码
	 * @author CaoJing
	 * @date 2021/05/18 00:28:28
	 */
	public static int getStatusCode(String url) {
		GetMethod get = new GetMethod(url);
		try {
			client.executeMethod(get);
			return get.getStatusCode();
		} catch (Exception ex) {
			logger.error(url, ex);
		} finally {
			get.releaseConnection();
		}
		return 500;
	}

	/**
	 * Put String
	 *
	 * @param url
	 * @param headers
	 * @param querys
	 * @param body
	 * @return String
	 * @throws Exception
	 */
	public static String doPut(String url, Map<String, String> headers,
							   Map<String, String> querys,
							   Object body) {

		PutMethod post = new PutMethod(url);
		NameValuePair[] nameValuePairs = buildNameValuePairs(querys);
		if (nameValuePairs != null) {
			post.setQueryString(nameValuePairs);
		}
		try {
			post.setRequestEntity(new StringRequestEntity(JSON.toJSONString(body), "application/json", "UTF-8"));
			client.executeMethod(post);
			if (post.getStatusCode() > 299) {
				return null;
			}
			return post.getResponseBodyAsString();
		} catch (Exception ex) {
			throw new RuntimeException("请求异常");
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Post 请求体请求
	 *
	 * @param url
	 * @param headers
	 * @param querys
	 * @param body
	 * @return String
	 * @throws Exception
	 */
	public static String post(String url, Map<String, String> headers,
							  Map<String, String> querys,
							  Object body) {

		PostMethod post = new PostMethod(url);
		NameValuePair[] nameValuePairs = buildNameValuePairs(querys);
		if (nameValuePairs != null) {
			post.setQueryString(nameValuePairs);
		}
		try {
			post.setRequestEntity(new StringRequestEntity(JSON.toJSONString(body), "application/json", "UTF-8"));
			client.executeMethod(post);
			if (post.getStatusCode() > 299) {
				return null;
			}
			return post.getResponseBodyAsString();
		} catch (Exception ex) {
			throw new RuntimeException("请求异常");
		} finally {
			post.releaseConnection();
		}
	}


	/**
	 * okhttp3
	 */
	public static String okHttpPost(String url,String body){
		okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");
		RequestBody requestBody = RequestBody.create(mediaType, body);
		Request request = new Request.Builder()
				.url(url)
				.method("POST", requestBody)
				.addHeader("Content-Type", "application/json")
				.build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			return response.body().string();
		} catch (IOException e) {
			logger.error("请求异常");
		}
		return null;
	}

	public static String okHttpDelete(String url){
		okhttp3.MediaType mediaType = okhttp3.MediaType.parse("text/plain");
		RequestBody body = RequestBody.create(mediaType, "");
		Request request = new Request.Builder()
				.url(url)
				.method("DELETE", body)
				.build();
		try {
			Response response = okHttpClient.newCall(request).execute();
			return response.body().string();
		} catch (IOException e) {
			logger.error("请求异常");
		}
		return null;
	}
}
