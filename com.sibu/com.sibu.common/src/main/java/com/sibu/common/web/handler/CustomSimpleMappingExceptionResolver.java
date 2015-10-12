package com.sibu.common.web.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.subject.Subject;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import com.sibu.common.util.HttpUtil;
import com.sibu.common.util.StringUtil;

/**
 * SpringMVC异常处理，页面请求返回view, 其他请求返回json
 * 
 */
public class CustomSimpleMappingExceptionResolver extends
		SimpleMappingExceptionResolver {
	public final String GEUST_EXCPTION="Attempting to perform a guest-only operation.  The current Subject is not a guest (they have been authenticated or remembered from a previous login).  Access denied.";
	public final String AUTH_EXCPTION="The current Subject is not authenticated.  Access denied.";
	@Override
	protected ModelAndView doResolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception e) {
		String viewName = determineViewName(e, request);
		String exmsg = e.getMessage();
		String msg = "非法链接!";
		//根据异常 判断是否权限问题
		if(!exmsg.equals(GEUST_EXCPTION)&&!exmsg.equals(AUTH_EXCPTION)){
			msg="数据异常!";
		}
		// Apply HTTP status code for error views, if specified.
		// Only apply it if we're processing a top-level request.
		if (!StringUtil.isNull(viewName)) {
			Integer statusCode = determineStatusCode(request, viewName);
			if (statusCode != null) {
				applyStatusCodeIfPossible(request, response, statusCode);
			}
			return getModelAndView(viewName, e, request);
		} else {
			ModelAndView mv = new ModelAndView();
			HttpUtil.writeError(response, msg, e);
			return mv;
		}

	}

}
