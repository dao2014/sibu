package com.sibu.common.util;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import com.sibu.common.massage.ResponseUtil;

public class HttpUtil {
	
	
	/**
	 * 输出json格式提示 
	 * @param response
	 */
	public static void writeSuccess(ServletResponse response){
		try{
			response.setContentType("application/json;charset=UTF-8");
	        response.setCharacterEncoding("utf-8");
	        response.getWriter().write(ResponseUtil.success());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 输出json格式提示 
	 * @param response
	 * @param msg 提示消息
	 */
	public static void writeSuccess(ServletResponse response,String msg){
		try{
			response.setContentType("application/json;charset=UTF-8");
	        response.setCharacterEncoding("utf-8");
	        response.getWriter().write(ResponseUtil.success(msg));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 输出错误提示
	 * @param response
	 */
	public static void writeError(ServletResponse response){
		try{
	        response.setContentType("application/json;charset=UTF-8");
	        response.setCharacterEncoding("utf-8");
	        response.getWriter().write(ResponseUtil.error());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * 输出错误提示
	 * @param response
	 * @param msg   带消息传送
	 */
	public static void writeError(ServletResponse response,String msg){
		try{
			response.setContentType("application/json;charset=UTF-8");
	        response.setCharacterEncoding("utf-8");
	        response.getWriter().write(ResponseUtil.error(msg));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
