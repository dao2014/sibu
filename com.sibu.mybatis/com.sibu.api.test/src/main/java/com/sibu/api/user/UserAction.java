package com.sibu.api.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sibu.common.bean.StatusBean;
import com.sibu.common.massage.ResponseUtil;
import com.sibu.common.mybatis.page.Pagination;
import com.sibu.common.mybatis.page.ParamUtil;
import com.sibu.common.util.JacksonUtil;
import com.sibu.common.util.Version;
import com.sibu.dao.user.bean.UserBean;
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
	public String list(String param, String page, String size, String version,HttpServletRequest req, HttpServletResponse res) throws Exception{
		if(Version.V10.equals(version)){
			Map<String, Object> map = new HashMap<String, Object>();
            map.put("id",param);
			Pagination<UserBean> result = userService.list(map, ParamUtil.getPage(page), ParamUtil.getPageSize(size));
			if(result != null && !CollectionUtils.isEmpty(result.getItems())){
				List<UserBean> voList = new ArrayList<UserBean>(result.getItems().size());
				for (UserBean da : result.getItems()) {
					
					voList.add(da);
				}
				Subject currentUser = SecurityUtils.getSubject();

				Session session = currentUser.getSession();
				String sessionid = session.getId()+"";
				return ResponseUtil.data(new Pagination<UserBean>(result.getCount(), result.getPage(), result.getSize(), voList));
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
	@RequestMapping(value = "/adduser", method = RequestMethod.POST)
	@ResponseBody
	public String AddUser(UserBean ub,String version,HttpServletRequest req, HttpServletResponse res) throws Exception{
		if(Version.V10.equals(version)){
//			UserBean _brean = JacksonUtil.fromJson(param, UserBean.class);
//			if(ub == null){
//				return ResponseUtil.error("参数错误");
//			}
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
	public String getUserInfo(String param,String version,HttpServletRequest req, HttpServletResponse res) throws Exception{
		if(Version.V10.equals(version)){
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("id", param);
			UserBean ub = userService.get(params);
			return ResponseUtil.data(ub);
		}
		return ResponseUtil.unknownVersion();
	}
	
	/**
	 * 登陆
	 * @return String
	 * @throws Exception
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	public String getUserInfo(String username,String password,String version,HttpServletRequest req, HttpServletResponse res) throws Exception{
		String exceptionClassName = (String)req.getAttribute("shiroLoginFailure");
		Collection<Session> sessions =  sessionDAO.getActiveSessions();
		sessionIdCookie.getSessionIdCookie();
		if(Version.V10.equals(version)){
			Map<String, Object> param = new  HashMap<String, Object>();
			param.put("name", username);
			param.put("pwd", password);
			StatusBean<UserBean> ub = userService.login(param);
			if(ub.getStatus())
				return ResponseUtil.data(ub.getData());
			else
				return ResponseUtil.error();
		}
		return ResponseUtil.unknownVersion();
	}
	
	/**
	 * 登陆失败
	 * @return String
	 * @throws Exception
	 */
	@RequestMapping(value = "/erron")
	@ResponseBody
	public String erron(String username,String password,String version,HttpServletRequest req, HttpServletResponse res) throws Exception{
		
		return ResponseUtil.error();
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
	public String editPOST(@JsonProperty("id") String name,@RequestBody UserBean userbrean,String id, String version, HttpServletRequest req, HttpServletResponse res) throws Exception{
		if(Version.V10.equals(version)){
			UserBean _brean = JacksonUtil.fromJson(id, UserBean.class);
			if(_brean == null){
				return ResponseUtil.error("address参数错误");
			}
			userService.update(_brean);
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
			userService.deleteById(id);
			return ResponseUtil.success(); 
		}
		return ResponseUtil.unknownVersion();
	}
	
}

