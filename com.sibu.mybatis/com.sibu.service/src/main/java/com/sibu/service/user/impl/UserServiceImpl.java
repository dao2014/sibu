package com.sibu.service.user.impl;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.RowBounds;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.sibu.common.bean.StatusBean;
import com.sibu.common.mybatis.SqlSessionFactoryBean;
import com.sibu.common.mybatis.page.PageUtil;
import com.sibu.common.mybatis.page.Pagination;
import com.sibu.common.mybatis.search.SearchTerm;
import com.sibu.common.service.impl.BaseServiceImpl;
import com.sibu.common.spring.MyDelegatingDataSource;
import com.sibu.common.util.JdbcTemplateDaoSupport;
import com.sibu.common.util.StringUtil;
import com.sibu.dao.user.bean.UserBean;
import com.sibu.dao.user.dao.UserMapper;
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
	MyDelegatingDataSource transactionManager;
	@Autowired
	SqlSessionFactoryBean sqlSessionFactory;
	@Resource
	UserMapper userMapper;
	@Autowired
    private PasswordHelper passwordHelper;
	@Autowired
	JdbcTemplate jdbcTemplate;
//	
	@Autowired  
    RedisTemplate<String, UserBean> redisTemplate;
	
	@Override
	public UserBean get(Map<String, Object> params) throws Exception {
		String username = (String)SecurityUtils.getSubject().getPrincipal();
		ValueOperations<String, UserBean> valueOper = redisTemplate.opsForValue();  
		UserBean ub= valueOper.get(username);  
		return userMapper.get(params);
	}
	
	
	public void testDatesource1(Map<String, Object> params ) throws ClassNotFoundException{
		BoneCPConfig config = new BoneCPConfig();
		//Class.forName("com.mysql.jdbc.Driver");
//		config.setDriverClass("com.mysql.jdbc.Driver");
		
		config.setJdbcUrl("jdbc:mysql://192.168.130.253/sy?useUnicode=true&amp;characterEncoding=utf8");
		config.setUsername("root");
		config.setPassword("123456");
		config.setMinConnectionsPerPartition(2);
		config.setMaxConnectionsPerPartition(2);
		config.setPartitionCount(1);
		BoneCPDataSource newDS = new BoneCPDataSource(config);
		newDS.setDriverClass("com.mysql.jdbc.Driver");
		
		NamedParameterJdbcTemplate jdbcTemplates = new NamedParameterJdbcTemplate(jdbcTemplate);
		String sql="select count(*) from user";
		int m=jdbcTemplates.getJdbcOperations().queryForInt(sql);
	}
	
	public void testDatesource2(Map<String, Object> params ) throws ClassNotFoundException{
		BoneCPConfig config = new BoneCPConfig();
		Class.forName("com.mysql.jdbc.Driver");
//		config.setDriverClass("com.mysql.jdbc.Driver");
		
		config.setJdbcUrl("jdbc:mysql://127.0.0.1/sy?useUnicode=true&amp;characterEncoding=utf8");
		config.setUsername("root");
		config.setPassword("123456");
		config.setMinConnectionsPerPartition(2);
		config.setMaxConnectionsPerPartition(2);
		config.setPartitionCount(1);
		BoneCPDataSource newDS = new BoneCPDataSource(config);
		newDS.setDriverClass("com.mysql.jdbc.Driver");
		jdbcTemplate.setDataSource(newDS);
		 String sql = "select * from user where name = '"+params.get("name")+"'";
		 List<UserBean> listContact = jdbcTemplate.query(sql, new RowMapper<UserBean>() {
	            public UserBean mapRow(ResultSet result, int rowNum) throws SQLException {
	            	UserBean contact = new UserBean();
	                String name = result.getString("id");
	                return contact;
	            }
	        });
	}
	
	@Override
	public UserBean getBeanById(Map<String, Object> params) throws Exception{
		return userMapper.get(params);
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
	public StatusBean<UserBean> login(Map<String, Object> params) throws Exception {
		if(!StringUtil.isNull(params)){
			String sql = " select * from user where name=:name ";
			UserBean user = new UserBean();
			user.setName(params.get("name")+"");
//			user.setPwd(params.get("pwd")+"");
//			passwordHelper.encryptPassword(user);
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
        if(JdbcTemplateDaoSupport.updateObjectOrAddOrDele(sql, bean)){
        	return success("操作成功！");
        }
		return error("增加失败"); 
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
