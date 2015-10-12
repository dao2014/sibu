package com.sibu.common.util;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.sibu.dao.user.vo.UserBean;

public class SessionUtil {
	private static Logger log = Logger.getLogger("SessionUtil");
	public static String getUserId(){
		String username = (String)SecurityUtils.getSubject().getPrincipal();
		return username;
	}
	
	
	/**
	 * 获取session 中的用户对象
	 * @return 用户对象
	 */
	public static UserBean getSessionUserInfo(){
		UserBean ub = null;
		RedisTemplate<String,UserBean> redisTemplate = SpringUtils.getBean("redisTemplate");
		ValueOperations<String, UserBean> valueOper = redisTemplate.opsForValue();
		if(!StringUtil.isNull(getUserId())){
			ub = valueOper.get(getUserId());
			if(StringUtil.isNull(ub)){
				log.info("没有发现缓存");
			}
		}else{
			log.info("用户登录失效或者没有登录");
		}
    	return ub;
	}
	
	/**
	 * 根据key 获取 redis里面的缓存数据
	 * @param key   缓存的key
	 * @return
	 */
	public static Object getSessionObject(String key){
		Object object = null;
		RedisTemplate<String,UserBean> redisTemplate = SpringUtils.getBean("redisTemplate");
		ValueOperations<String, UserBean> valueOper = redisTemplate.opsForValue();
		if(!StringUtil.isNull(getUserId())){
			object = valueOper.get(getUserId());
			if(StringUtil.isNull(object)){
				log.info("没有发现缓存");
			}
		}else{
			log.info("出错");
		}
    	return object;
	}
}
