package com.sibu.common.massage;

import com.sibu.common.util.JacksonUtil;

/**
 * 用于生成接口调用响应消息体
 *
 * @author linwh
 */
public class ResponseUtil {
	
	/**
	 * 请求成功，返回数据消息
	 * 
	 * @param object 数据
	 * @return
	 */
	public static String data(Object object){
		
		Response resp = new Response(Status.SUCCESS, null, object);
		
		return JacksonUtil.toJson(resp);
	}
	
	/**
	 * 请求成功
	 * 
	 */
	public static String success(){
		
		Response resp = new Response(Status.SUCCESS, null, null);
		
		return JacksonUtil.toJson(resp);
	}
	
	/**
	 * 请求成功带参数提示
	 * @param msg
	 * @return
	 */
	public static String success(String msg){
		
		Response resp = new Response(Status.SUCCESS, msg, null);
		
		return JacksonUtil.toJson(resp);
	}
	
	/**
	 * 请求成功，无返回数据
	 * 
	 * @return
	 */
	public static String warn(){
		
		Response resp = new Response(Status.WARN, null);
		
		return JacksonUtil.toJson(resp);
	}
	
	/**
	 * 请求出错
	 * 
	 * @return
	 */
	public static String error(){
		
		Response resp = new Response(Status.ERROR, null);
		
		return JacksonUtil.toJson(resp);
	}
	
	/**
	 * 请求出错，返回错误消息
	 * 
	 * @param msg 消息内容
	 * @return
	 */
	public static String error(String msg){
		
		Response resp = new Response(Status.ERROR, msg);
		
		return JacksonUtil.toJson(resp);
	}
	
	private static final String unkownVersion = "未知版本号";
	
	/**
	 * 版本号错误
	 * 
	 * @return
	 */
	public static String unknownVersion(){
		
		Response resp = new Response(Status.ERROR, unkownVersion);
		
		return JacksonUtil.toJson(resp);
	}

	/**
	 * 返回提示消息
	 * 
	 * @param msg 消息内容
	 * @return
	 */
	public static String info(String msg){
		
		Response resp = new Response(Status.INFO, msg);
		
		return JacksonUtil.toJson(resp);
	}
	
}
