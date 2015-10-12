package com.sibu.api.user;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sibu.common.bean.StatusBean;
import com.sibu.common.massage.ResponseUtil;
import com.sibu.common.util.SessionUtil;
import com.sibu.common.util.StringUtil;
import com.sibu.common.util.Version;
import com.sibu.dao.user.vo.UserBean;
import com.sibu.service.user.UserService;

/**
 * UserAction
 * @version 1.0
 * @author dao
 */
@Controller("m_login")
@RequestMapping(value = "/user",produces="application/json")
public class UserAction {
	
	/** 用户服务接口 */
	@Resource
	private UserService userService;
	@Autowired 
    private SessionDAO sessionDAO;
	@Autowired
	private DefaultWebSessionManager sessionIdCookie;
	
	/**
	 * 查询
	 * @param param  参数值
	 * @param page   页码
	 * @param size   页数
	 * @param version  接口版本号
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/list",method = RequestMethod.GET)
	@ResponseBody
	//匿名访问
	@RequiresGuest   
	public String list(String version,HttpServletRequest req, HttpServletResponse res) throws Exception{
		if(Version.V10.equals(version)){
			List<UserBean> list = userService.list(null);
			return ResponseUtil.data(list);
		}
		return ResponseUtil.unknownVersion();
	}
	
	/**
	 * 增加
	 * @return String
	 * @throws Exception
	 */
	@RequestMapping(value = "/adduser", method = RequestMethod.POST)
	@ResponseBody
	public String AddUser(UserBean ub,String version,HttpServletRequest req, HttpServletResponse res) throws Exception{
		if(Version.V10.equals(version)){
			userService.save(ub);
			return ResponseUtil.success(); 
		}
		return ResponseUtil.unknownVersion();
	}
	
	/**
	 * 获取 对象
	 * @return String
	 * @throws Exception
	 */
	@RequestMapping(value = "/get", method = RequestMethod.GET) 
	@ResponseBody 
	//登陆之后访问
	@RequiresAuthentication
	public String getUserInfo(String param,String version,HttpServletRequest req, HttpServletResponse res) throws Exception{
		if(Version.V10.equals(version)){
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("name", param);
			if(!StringUtil.isNull(params)){
				UserBean ub = userService.get(params);
				return ResponseUtil.data(ub);
			}else{
				return ResponseUtil.error();
			}
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
	@ResponseBody
	public String editPOST(String password,String version, HttpServletRequest req, HttpServletResponse res) throws Exception{
		if(Version.V10.equals(version)){
			UserBean brean = SessionUtil.getSessionUserInfo();
			if(brean!=null){
				brean.setPwd(password);
				StatusBean sb= userService.update(SessionUtil.getSessionUserInfo());
				if(sb.getStatus()){
					return ResponseUtil.success();
				}else{
					return ResponseUtil.error();
				}
			}else{
				return ResponseUtil.error();
			}
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
	@ResponseBody
	public String del(String id,String version, HttpServletResponse res) throws Exception{
		if(Version.V10.equals(version)){
			userService.deleteById(id);
			return ResponseUtil.success(); 
		}
		return ResponseUtil.unknownVersion();
	}
	
}

