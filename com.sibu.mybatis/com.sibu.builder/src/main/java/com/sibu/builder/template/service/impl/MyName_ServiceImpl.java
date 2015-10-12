package com.sibu.builder.template.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.sibu.builder.template.bean.MyName_Bean;
import com.sibu.builder.template.dao.MyName_Mapper;
import com.sibu.builder.template.service.MyName_Service;
import com.sibu.common.bean.StatusBean;
import com.sibu.common.mybatis.page.PageUtil;
import com.sibu.common.mybatis.page.Pagination;
import com.sibu.common.mybatis.search.SearchTerm;
import com.sibu.common.service.impl.BaseServiceImpl;

/**
 * MyName_ServiceImpl
 * @author 作者_
 **/
@Service("myName_Service")
public class MyName_ServiceImpl extends BaseServiceImpl implements MyName_Service{
	@Resource
	MyName_Mapper myName_Mapper;
	
	@Override
	public MyName_Bean getBeanById(String id) throws Exception{
		return myName_Mapper.get(id);
	}
	@Override
	public List<MyName_Bean> listAll() throws Exception {
		return myName_Mapper.list(null);
	}
	@Override
	public int count(MyName_Bean brean) throws Exception {
		return myName_Mapper.count(brean);
	}
	@Override
	public List<MyName_Bean> list(Map<String, Object> params) throws Exception {
		return myName_Mapper.list(params);
	}
	@Override
	public Pagination<MyName_Bean> list(Map<String, Object> params,int page, int size) throws Exception {
		MyName_Bean brean = new MyName_Bean();
		//需要设置ID
		int count = myName_Mapper.count(brean);
		if(count > 0){
			List<MyName_Bean> items = list(params,new SearchTerm(page, size));
			if(!CollectionUtils.isEmpty(items)){
				return new Pagination<MyName_Bean>(count, page, size, items);
			}
		}
		return null;
	}
	@Override
	public StatusBean<?> save(MyName_Bean bean) throws Exception {
		Date currDate = new Date();
		//TODO 处理业务
		myName_Mapper.save(bean);
		return success("操作成功！");
	}
	@Override
	public StatusBean<?> update(MyName_Bean bean) throws Exception {
		Date currDate = new Date();
		HashMap<String, Object> params = new HashMap<String, Object>();
		//TODO 处理业务
		myName_Mapper.update(params);
		return success("操作成功！");
	}
	@Override
	public StatusBean<?> deleteById(String id) throws Exception {
		myName_Mapper.delete(id);
		return success("操作成功！");
	}
	@Override
	public StatusBean<?> batchDelete(String ids) throws Exception {
		myName_Mapper.batchDelete(Arrays.asList(ids.split(",")));
		return success("操作成功！");
	}
	
	/**
	 * 
	 * @param id
	 * @param st
	 * @return
	 */
	public List<MyName_Bean> list(Map<String, Object> params, SearchTerm st) {
		RowBounds rowBounds = null;
		if(st != null){
			rowBounds = PageUtil.getRowBounds(st.getPage(), st.getSize());
		}
		return myName_Mapper.list(rowBounds, params);
	}
	
}