package com.sibu.api.user;

import java.util.HashMap;
import java.util.Map;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.sibu.common.util.JdbcTemplateDaoSupport;
import com.sibu.common.util.SpringUtils;
import com.sibu.common.util.StringUtil;
import com.sibu.dao.user.bean.UserBean;
import com.sibu.service.user.UserService;

/**
 * <p>User: dao
 * <p>Date: 14-1-28
 * <p>Version: 1.0
 */
public class UserRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;
//
    
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = (String)principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        System.out.println(username);
        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

       String username = (String)token.getPrincipal();
       System.out.println(username);
       UserBean user=null;
		try {
			Map<String, Object> param = new  HashMap<String, Object>();
			param.put("name", username);
			user = userService.login(param).getData();
 		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if(user == null) {
            throw new UnknownAccountException();//没找到帐号
        }else{
        	RedisTemplate<String,UserBean> redisTemplate = SpringUtils.getBean("redisTemplate");
        	ValueOperations<String, UserBean> valueOper = redisTemplate.opsForValue();
        	valueOper.set(user.getName(), user); 
        }
        //交给AuthenticatingRealm使用CredentialsMatcher进行密码匹配，如果觉得人家的不好可以自定义实现
          SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                user.getName(), //用户名
                user.getPwd(), //密码
                ByteSource.Util.bytes(user.getCredentialsSalt()),//salt=username+salt
                getName()  //realm name
        );
        return authenticationInfo;
    }

    @Override
    public void clearCachedAuthorizationInfo(PrincipalCollection principals) {
        super.clearCachedAuthorizationInfo(principals);
    }

    @Override
    public void clearCachedAuthenticationInfo(PrincipalCollection principals) {
        super.clearCachedAuthenticationInfo(principals);
    }

    @Override
    public void clearCache(PrincipalCollection principals) {
        super.clearCache(principals);
    }

    public void clearAllCachedAuthorizationInfo() {
        getAuthorizationCache().clear();
    }

    public void clearAllCachedAuthenticationInfo() {
        getAuthenticationCache().clear();
    }

    public void clearAllCache() {
        clearAllCachedAuthenticationInfo();
        clearAllCachedAuthorizationInfo();
    }

}
