package com.sibu.builder.template.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.sibu.builder.template.bean.MyName_Bean;
import com.sibu.builder.template.service.MyName_Service;
import com.sibu.common.bean.StatusBean;
import com.sibu.common.service.impl.BaseServiceImpl;
import com.sibu.common.util.JdbcTemplateDaoSupport;
import com.sibu.common.util.StringUtil;
import com.sibu.dao.user.vo.UserBean;

/**
 * MyName_ServiceImpl
 * @author 作者_
 **/
@Service("myName_Service")
public class MyName_ServiceImpl extends BaseServiceImpl implements MyName_Service{
	
	
	@Override
	public MyName_Bean getBeanById(String id) throws Exception{
		MyName_Bean bean = new MyName_Bean();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", id);
		String sql="";
		bean = JdbcTemplateDaoSupport.getObjectDateInfo(sql, params, bean);
		return bean;
	}
	@Override
	public List<MyName_Bean> listAll() throws Exception {
		MyName_Bean bean = new MyName_Bean();
		String sql = " ";
		return JdbcTemplateDaoSupport.getList(sql, bean);
	}
	@Override
	public int count(MyName_Bean brean) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		String sql = "";
		return JdbcTemplateDaoSupport.count(sql, params);
	}
	@Override
	public List<MyName_Bean> list(Map<String, Object> params) throws Exception {
		MyName_Bean bean = new MyName_Bean();
		String sql = "";
		return JdbcTemplateDaoSupport.getList(sql, bean);
	}
	 
	@Override
	public StatusBean<?> save(MyName_Bean bean) throws Exception {
		Date currDate = new Date();
		String sql="";
		//TODO 处理业务
//		bean.setId(StringUtil.getUUID());
		if(JdbcTemplateDaoSupport.updateOrAddOrDele(sql, bean)){
        	return success("操作成功！");
        }
		return error("增加失败"); 
	}
	@Override
	public StatusBean<?> update(MyName_Bean bean) throws Exception {
		Date currDate = new Date();
		String sql="";
		if(JdbcTemplateDaoSupport.updateOrAddOrDele(sql, bean)){
			return success("操作成功！");
		}else{
			return error("修改失败");
		}
	}
	@Override
	public StatusBean<?> deleteById(String id) throws Exception {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("id", id);
		String sql ="";
		if(JdbcTemplateDaoSupport.updateOrAddOrDele(sql, params)){
			return success("操作成功！");
		}else{
			return error("修改失败");
		}
	}
	@Override
	public MyName_Bean get(Map<String, Object> params) throws Exception {
		MyName_Bean bean = new MyName_Bean();
		String sql = "";
		bean = JdbcTemplateDaoSupport.getObjectDateInfo(sql, params, bean);
		return bean;
	}
	
}