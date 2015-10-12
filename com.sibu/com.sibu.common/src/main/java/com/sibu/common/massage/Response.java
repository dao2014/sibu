package com.sibu.common.massage;

import java.io.Serializable;

/**
 * 接口调用响应体
 *
 * @author linwh
 */
public class Response implements Serializable{
	
	
	private boolean success;//是否成功
	
	private String status;//响应码
	
	private String msg;//消息
	
	private Object data;//响应数据
	
	public Response(){}
	
	public Response(String status, String msg){
		
		if(status.equals(Status.SUCCESS))
			this.success = true;
		
		this.status = status;
		this.msg = msg;
	}
	
	public Response(String status, String msg, Object data){
		
		if(status.equals(Status.SUCCESS))
			this.success = true;
		
		this.status = status;
		this.msg = msg;
		this.data = data;
	}
	

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	
}
