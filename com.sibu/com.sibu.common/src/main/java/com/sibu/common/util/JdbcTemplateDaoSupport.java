package com.sibu.common.util;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.sibu.common.log.LogMessage;
import com.sibu.dao.user.vo.UserBean;

public class JdbcTemplateDaoSupport {
	private static Logger log = Logger.getLogger("JdbcTemplateDaoSupport");
	
	/**
	 * 获取数据源配置
	 * @return
	 */
	private static BoneCPDataSource getUserDateSource(){
		BoneCPDataSource boneCPDataSource = SpringUtils.getBean("dataSource");
		return boneCPDataSource;
	}
	
	/**
	 * 从spring 获取 jdbc
	 * @return
	 */
	private static JdbcTemplate getJdbcTemplate(){
		return SpringUtils.getBean("jdbcTemplate");
	}
	
	/**
	 * 获取默认jdbc
	 * @return
	 */
	private static NamedParameterJdbcTemplate getDefoultDateSource(){
		BoneCPDataSource bcd = getUserDateSource();
		JdbcTemplate jdbcTemplate = getJdbcTemplate();
		jdbcTemplate.setDataSource(bcd);
		NamedParameterJdbcTemplate npjdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
		return npjdbc;
	}
	
	/**
	 * 获取 默认数据的用户信息
	 * @param sql  例如: select * from user where id=:id and db_user_name=:dbUserName
	 * @param obj  用户对象
	 * @param mappedClass  用户对象class文件
 	 * @return
	 */
	public static <T> Object getUserDate(String sql,T obj){
		NamedParameterJdbcTemplate npjdbc = getDefoultDateSource();
		SqlParameterSource ps=new BeanPropertySqlParameterSource(obj);
		try {
			obj = (T) npjdbc.queryForObject(sql, ps, new BeanPropertyRowMapper(obj.getClass()));
			if(StringUtil.isNull(obj)){
				log.info("没有获取到数据"+obj);
			}
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			log.error("获取当前用户信息异常",e.getRootCause());
			e.printStackTrace();
		}
		return obj;
	}
	
	
	/**
	 * 获取用户当前的JDBC
	 * @param sql
	 * @param params
	 * @param obj
	 * @param mappedClass
	 * @return
	 */
	private static NamedParameterJdbcTemplate  getUserJDBC(){
		UserBean ub = SessionUtil.getSessionUserInfo();
		NamedParameterJdbcTemplate jpbj = null;
		BoneCPDataSource newDateSource = null;
		if(!StringUtil.isNull(ub)){
			try {
				BoneCPConfig jdbcconfig = new BoneCPConfig();
				JdbcTemplate jdbc = getJdbcTemplate();
				jdbcconfig.setJdbcUrl(ub.getDbUrl());
				jdbcconfig.setUsername(ub.getDbUserName());
				jdbcconfig.setPassword(ub.getDbUserPassWord());
				jdbcconfig.setMinConnectionsPerPartition(2);
				jdbcconfig.setMaxConnectionsPerPartition(2);
				jdbcconfig.setPartitionCount(1);
				newDateSource = new BoneCPDataSource(jdbcconfig);
				Class.forName("com.mysql.jdbc.Driver");
				jdbc.setDataSource(newDateSource);
				jpbj = new NamedParameterJdbcTemplate(jdbc);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error(LogMessage.MESSAGE_WARNING_0, e.getCause());
				e.printStackTrace();
			}
		}else{
			log.info(LogMessage.MESSAGE_WARNING_1);
			jpbj = getDefoultDateSource();
		}
		return jpbj;
	}
	
	/**
	 * 查询 当前用户对应的数据库 数据.
	 * @param sql 
	 * @param params 参数 对象 
	 * @param obj    需要查询的对象
	 * @return       返回要查询的对象结果
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getObjectDateInfo(String sql,T obj){
		NamedParameterJdbcTemplate npjdbc = getUserJDBC();
		SqlParameterSource ps=new BeanPropertySqlParameterSource(obj);
		try {
			obj = (T) npjdbc.queryForObject(sql, ps, new BeanPropertyRowMapper(obj.getClass()));
			if(StringUtil.isNull(obj)){
				log.info(LogMessage.DATE_MESSAGE_INFO_0+"==>"+sql+"==>"+obj.getClass());
			}
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			log.error(LogMessage.DATE_MESSAGE_ERROR_0+"==>"+sql+"==>"+obj.getClass(),e.getRootCause());
			e.printStackTrace();
		}
		return obj;
	}
	
	
	/**
	 * 查询 当前用户对应的数据库 数据.
	 * @param sql 
	 * @param params 参数 Map 
	 * @param obj    需要查询的对象
	 * @return       返回要查询的对象结果
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getObjectDateInfo(String sql,Map<String, Object> params,T obj){
		NamedParameterJdbcTemplate npjdbc = getUserJDBC();
		SqlParameterSource ps=new BeanPropertySqlParameterSource(obj);
		try {
			obj = (T) npjdbc.queryForObject(sql, params, new BeanPropertyRowMapper(obj.getClass()));
			if(StringUtil.isNull(obj)){
				log.info(LogMessage.DATE_MESSAGE_INFO_0+"==>"+sql+"==>"+obj.getClass());
			}
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			log.error(LogMessage.DATE_MESSAGE_ERROR_0+"==>"+sql+"==>"+obj.getClass(),e.getRootCause());
			e.printStackTrace();
		}
		return obj;
	}
	
	/**
	 * 增删改 格式为:
	 * @param sql delete stu where s_id=:id或者 insert into stu values(:sname,:ssex,:sbrith)  
	 * @param params Map 类型
	 * @return
	 */
	public static boolean updateOrAddOrDele(String sql,Map<String, Object> params){
		boolean status=false;
		NamedParameterJdbcTemplate npjdbc = getUserJDBC();
		try {
			if(npjdbc.update(sql, params)>=1){
				status = true;
			}
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			log.error(LogMessage.DATE_UPDATE_ERROR_0+"==>"+sql,e.getRootCause());
			e.printStackTrace();
		}
		return status;
	}
	
	/**
	 * 更新数据修改删除 格式为:
	 * @param <T>
	 * @param sql delete stu where s_id=:id或者 insert into stu values(:sname,:ssex,:sbrith)  
	 * @param ob  更新的对象
	 * @return
	 */
	public static <T> boolean updateOrAddOrDele(String sql, T ob){
		boolean status=false;
		NamedParameterJdbcTemplate npjdbc = getUserJDBC();
		try {
			//:后面的名称必须和stu属性名称一样
			SqlParameterSource ps=new BeanPropertySqlParameterSource(ob);
			if(npjdbc.update(sql, ps)>=1){
				status = true;
			}
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			log.error(LogMessage.DATE_UPDATE_ERROR_0+"==>"+sql,e.getRootCause());
			e.printStackTrace();
		}
		return status;
	}
	
	/**
	 * 获取数据列表  map方式传参
	 * @param sql
	 * @param params  参数
	 * @param brean   返回列表结果对象
	 * @return        返回对象
	 */
	public static <T> List<T> getList(String sql,Map<String, Object> params,T brean){
		NamedParameterJdbcTemplate npjdbc = getUserJDBC();
		List<T> T = (List<T>) npjdbc.queryForList(sql, params, brean.getClass());
		return T;
	}
	/**
	 * 获取数据列表: 对象传参
	 * @param sql    sql 语句
	 * @param brean  需要查询的条件对象
	 * @return       需要返回的结果集
	 */
	public static <T> List<T> getList(String sql,T brean){
		NamedParameterJdbcTemplate npjdbc = getUserJDBC();
		SqlParameterSource ps=new BeanPropertySqlParameterSource(brean);
		List<T> T = npjdbc.query(sql, ps, new BeanPropertyRowMapper(brean.getClass()));
		return T;
	}
	
	/**
	 * 获取行数,或者 int类型的数
	 * @param sql  		sql语句 
	 * @param paramMap  查询条件
	 * @return
	 */
	public static int count(String sql,Map<String, Object> paramMap){
		NamedParameterJdbcTemplate npjdbc = getUserJDBC();
		return npjdbc.queryForInt(sql, paramMap);
	}
	
}
