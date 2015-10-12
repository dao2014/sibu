package com.sibu.api.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sibu.common.bean.StatusBean;
import com.sibu.common.massage.ResponseUtil;
import com.sibu.common.util.Version;
import com.sibu.dao.user.bean.UserBean;
import com.sibu.service.user.UserService;



@Controller
@RequestMapping(value = "/",produces="application/json")
public class UserLoginAction {
	
	/** 用户服务接口 */
	@Resource
	private UserService userService;
	@Autowired
    private SessionDAO sessionDAO;
	@Autowired
	private DefaultWebSessionManager sessionIdCookie;
	
	/**
	 * 登陆
	 * 
	 * @return String
	 * @throws Exception
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	public String getUserInfo(String username, String password, String version,
			HttpServletRequest req, HttpServletResponse res) throws Exception {
		if (Version.V10.equals(version)) {
			String exceptionClassName = (String) req
					.getAttribute("shiroLoginFailure");
			String error = null;
			if (UnknownAccountException.class.getName().equals(
					exceptionClassName)) {
				error = "用户名/密码错误";
			} else if (IncorrectCredentialsException.class.getName().equals(
					exceptionClassName)) {
				error = "用户名/密码错误";
			} else if (exceptionClassName != null) {
				error = "其他错误：" + exceptionClassName;
			}
			if(error!=null)
				return ResponseUtil.error(error);
			else{
				return ResponseUtil.success();
			}
		}
		return ResponseUtil.unknownVersion();
	}
}
