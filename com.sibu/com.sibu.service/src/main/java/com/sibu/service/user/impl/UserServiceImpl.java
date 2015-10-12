package com.sibu.service.user.impl;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.jolbox.bonecp.BoneCPDataSource;
import com.sibu.common.bean.StatusBean;
import com.sibu.common.service.impl.BaseServiceImpl;
import com.sibu.common.util.JdbcTemplateDaoSupport;
import com.sibu.common.util.StringUtil;
import com.sibu.dao.user.vo.UserBean;
import com.sibu.service.user.UserService;

/**
 * UserServiceImpl
 * @author dao
 **/
@Service("userService")
public class UserServiceImpl extends BaseServiceImpl implements UserService{
	
	@Autowired
	BoneCPDataSource dataSource;
	@Autowired
    private PasswordHelper passwordHelper;
	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired  
    RedisTemplate<String, UserBean> redisTemplate;
	
	@Override
	public UserBean get(Map<String, Object> params) throws Exception {
		UserBean ub = new UserBean();
		String sql = "select * from user where name=:name";
		ub = JdbcTemplateDaoSupport.getObjectDateInfo(sql, params, ub);
		return ub;
	}
	
	
	@Override
	public List<UserBean> list(Map<String, Object> params) throws Exception {
		UserBean brean = new UserBean();
		String sql = "select * from user where 1=1";
		return JdbcTemplateDaoSupport.getList(sql, brean);
	}
	
	@Override
	public UserBean getBeanById(Map<String, Object> params) throws Exception{
		UserBean brean = new UserBean();
		String sql = "select * from user where name=:name";
		brean = JdbcTemplateDaoSupport.getObjectDateInfo(sql, params, brean);
		return brean;
	}
	@Override
	public List<UserBean> listAll() throws Exception {
		return null;
	}
	@Override
	public int count(UserBean brean) throws Exception {
		return 0;
	}
	
	 
	@Override
	public StatusBean<UserBean> login(Map<String, Object> params) throws Exception {
		if(!StringUtil.isNull(params)){
			String sql = " select * from user where name=:name ";
			UserBean user = new UserBean();
			user.setName(params.get("name")+"");
			user = (UserBean) JdbcTemplateDaoSupport.getUserDate(sql,  user);
			if(StringUtil.isNull(user)){
				return error("登陆失败!"); 
			}
			return success("操作成功！",user);
		}else{
			return error("参数不能为空"); 
		}
	}
	@Override
	public StatusBean<?> save(UserBean bean) throws Exception {
		Date currDate = new Date();
		String sql = "INSERT INTO  user  ( ID ,  name ,  pwd ,  create_date ,  salt ,  db_driver ,  db_url ,  db_user_name ,  db_user_pass_word ) VALUES "
				+ "(:id,:name,:pwd ,:createDate,:salt,:dbDriver,:dbUrl ,:dbUserName,:dbUserPassWord)";
		//加密密码
        passwordHelper.encryptPassword(bean);
        bean.setCreateDate(currDate);
        bean.setId(StringUtil.getUUID());
        if(JdbcTemplateDaoSupport.updateOrAddOrDele(sql, bean)){
        	return success("操作成功！");
        }
		return error("增加失败"); 
	}
	@Override
	public StatusBean<?> update(UserBean bean) throws Exception {
		passwordHelper.encryptPassword(bean);
		String sql = "UPDATE user SET pwd = :pwd,salt=:salt WHERE id = :id";
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
		String sql ="delete from user where id=:id";
		if(JdbcTemplateDaoSupport.updateOrAddOrDele(sql, params)){
			return success("操作成功！");
		}else{
			return error("修改失败");
		}
	}
	@Override
	public StatusBean<?> batchDelete(String ids) throws Exception {
		return success("操作成功！");
	}
	
}
