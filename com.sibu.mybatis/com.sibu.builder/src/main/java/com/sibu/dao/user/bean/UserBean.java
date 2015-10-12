package com.sibu.dao.user.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * UserBean
 * @version 1.0
 * @author dao
 */
public class UserBean implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String id;
	private Date createdatetime;
	private Date modifydatetime;
	private String name;
	private String pwd;
	private String createDate;
	private String updateTime;

	public String getId(){
		return id;
	}
	public void setId(String id){
		this.id = id;
	}
	public Date getCreatedatetime(){
		return createdatetime;
	}
	public void setCreatedatetime(Date createdatetime){
		this.createdatetime = createdatetime;
	}
	public Date getModifydatetime(){
		return modifydatetime;
	}
	public void setModifydatetime(Date modifydatetime){
		this.modifydatetime = modifydatetime;
	}
	public String getName(){
		return name;
	}
	public void setName(String name){
		this.name = name;
	}
	public String getPwd(){
		return pwd;
	}
	public void setPwd(String pwd){
		this.pwd = pwd;
	}
	public String getCreateDate(){
		return createDate;
	}
	public void setCreateDate(String createDate){
		this.createDate = createDate;
	}
	public String getUpdateTime(){
		return updateTime;
	}
	public void setUpdateTime(String updateTime){
		this.updateTime = updateTime;
	}

}

