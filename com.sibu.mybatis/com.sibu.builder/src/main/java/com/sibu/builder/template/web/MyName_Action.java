package com.sibu.builder.template.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sibu.builder.template.bean.MyName_Bean;
import com.sibu.builder.template.service.MyName_Service;
import com.sibu.common.massage.ResponseUtil;
import com.sibu.common.mybatis.page.Pagination;
import com.sibu.common.mybatis.page.ParamUtil;
import com.sibu.common.util.JacksonUtil;
import com.sibu.common.util.Version;

/**
 * MyName_Action
 * @version 1.0
 * @author 作者_
 */
@Controller
@RequestMapping(value = "/login",produces="application/json")
public class MyName_Action {
	
	/** 用户服务接口 */
	@Resource
	MyName_Service myName_Service;
	
	/**
	 * 查询分页
	 * @param param  参数值
	 * @param page   页码
	 * @param size   页数
	 * @param version  接口版本号
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/list",method = RequestMethod.GET)
	@ResponseBody
	public String list(String id, String page, String size, String version,HttpServletRequest req, HttpServletResponse res) throws Exception{
		if(Version.V10.equals(version)){
			Map<String, Object> map = new HashMap<String, Object>();
            map.put("id",id);
			Pagination<MyName_Bean> result = myName_Service.list(map, ParamUtil.getPage(page), ParamUtil.getPageSize(size));
			if(result != null && !CollectionUtils.isEmpty(result.getItems())){
				List<MyName_Bean> voList = new ArrayList<MyName_Bean>(result.getItems().size());
				for (MyName_Bean da : result.getItems()) {
					voList.add(da);
				}
				return ResponseUtil.data(new Pagination<MyName_Bean>(result.getCount(), result.getPage(), result.getSize(), voList));
			}
			return ResponseUtil.warn();
		}
		return ResponseUtil.unknownVersion();
	}
	
	/**
	 * 增加
	 * @return String
	 * @throws Exception
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public String addGET(String param,String version,HttpServletRequest req, HttpServletResponse res) throws Exception{
		if(Version.V10.equals(version)){
			MyName_Bean _brean = JacksonUtil.fromJson(param, MyName_Bean.class);
			if(_brean == null){
				return ResponseUtil.error("参数错误");
			}
			myName_Service.update(_brean);
			return ResponseUtil.success(); 
		}
		
		return ResponseUtil.unknownVersion();
	}
	 
	/**
	 * 保存编辑数据
	 * @param bean
	 * @param rel
	 * @param model
	 * @param req
	 * @param res
	 * @return String
	 * @throws Exception
	 */
	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public String editPOST(String id, String version, HttpServletRequest req, HttpServletResponse res) throws Exception{
		if(Version.V10.equals(version)){
			MyName_Bean _brean = JacksonUtil.fromJson(id, MyName_Bean.class);
			if(_brean == null){
				return ResponseUtil.error("address参数错误");
			}
			myName_Service.update(_brean);
			return ResponseUtil.success();
		}
		return ResponseUtil.unknownVersion();
	}
	/**
	 * 删除数据
	 * @param id
	 * @param res
	 * @throws Exception
	 */
	@RequestMapping(value = "/del",method = RequestMethod.POST)
	public String del(String id,String version, HttpServletResponse res) throws Exception{
		if(Version.V10.equals(version)){
			myName_Service.deleteById(id);
			return ResponseUtil.success(); 
		}
		return ResponseUtil.unknownVersion();
	}
	
}
