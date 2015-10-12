package com.sibu.common.massage;

/**
 * 
 * 接口调用响应码
 *
 * @author linwh
 */
public interface Status {
	
	final String ERROR = "00"; //错误
	
	final String SUCCESS = "01";//成功
	
	final String WARN = "02";//成功，无数据
	
	final String INFO = "03"; //提示信息
	
}
