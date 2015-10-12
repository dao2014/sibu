package com.sibu.dao.user.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.sibu.dao.user.bean.UserBean;
import com.sibu.dao.user.dao.UserMapper;
import com.sibu.dao.user.service.UserService;
import com.sibu.common.bean.StatusBean;
import com.sibu.common.mybatis.page.PageUtil;
import com.sibu.common.mybatis.page.Pagination;
import com.sibu.common.mybatis.search.SearchTerm;
import com.sibu.common.service.impl.BaseServiceImpl;

/**
 * UserServiceImpl
 * @author dao
 **/
@Service("userService")
public class UserServiceImpl extends BaseServiceImpl implements UserService{
	@Resource
	UserMapper userMapper;
	
	@Override
	public UserBean getBeanById(String id) throws Exception{
		return userMapper.get(id);
	}
	@Override
	public List<UserBean> listAll() throws Exception {
		return userMapper.list(null);
	}
	@Override
	public int count(UserBean brean) throws Exception {
		return userMapper.count(brean);
	}
	@Override
	public List<UserBean> list(Map<String, Object> params) throws Exception {
		return userMapper.list(params);
	}
	@Override
	public Pagination<UserBean> list(Map<String, Object> params,int page, int size) throws Exception {
		UserBean brean = new UserBean();
		//需要设置ID
		int count = userMapper.count(brean);
		if(count > 0){
			List<UserBean> items = list(params,new SearchTerm(page, size));
			if(!CollectionUtils.isEmpty(items)){
				return new Pagination<UserBean>(count, page, size, items);
			}
		}
		return null;
	}
	@Override
	public StatusBean<?> save(UserBean bean) throws Exception {
		Date currDate = new Date();
		//TODO 处理业务
		userMapper.save(bean);
		return success("操作成功！");
	}
	@Override
	public StatusBean<?> update(UserBean bean) throws Exception {
		Date currDate = new Date();
		HashMap<String, Object> params = new HashMap<String, Object>();
		//TODO 处理业务
		userMapper.update(params);
		return success("操作成功！");
	}
	@Override
	public StatusBean<?> deleteById(String id) throws Exception {
		userMapper.delete(id);
		return success("操作成功！");
	}
	@Override
	public StatusBean<?> batchDelete(String ids) throws Exception {
		userMapper.batchDelete(Arrays.asList(ids.split(",")));
		return success("操作成功！");
	}
	
	/**
	 * 
	 * @param id
	 * @param st
	 * @return
	 */
	public List<UserBean> list(Map<String, Object> params, SearchTerm st) {
		RowBounds rowBounds = null;
		if(st != null){
			rowBounds = PageUtil.getRowBounds(st.getPage(), st.getSize());
		}
		return userMapper.list(rowBounds, params);
	}
	
}
