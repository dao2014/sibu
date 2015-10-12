package com.sibu.dao.user.vo;

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
	private Date createDate;
	private String updateTime;
	private String salt;
	private String dbDriver;
	private String dbUrl;
	private String dbUserName;
	private String dbUserPassWord;

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
	 
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getUpdateTime(){
		return updateTime;
	}
	public void setUpdateTime(String updateTime){
		this.updateTime = updateTime;
	}
	public String getSalt() {
		return salt;
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}
	
	public String getCredentialsSalt() {
        return name + salt;
    }
	public String getDbDriver() {
		return dbDriver;
	}
	public void setDbDriver(String dbDriver) {
		this.dbDriver = dbDriver;
	}
	 
	public String getDbUserName() {
		return dbUserName;
	}
	public void setDbUserName(String dbUserName) {
		this.dbUserName = dbUserName;
	}
	public String getDbUserPassWord() {
		return dbUserPassWord;
	}
	public void setDbUserPassWord(String dbUserPassWord) {
		this.dbUserPassWord = dbUserPassWord;
	}
	public String getDbUrl() {
		return dbUrl;
	}
	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

}

