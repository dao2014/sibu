package com.sibu.common.web.realm;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

import com.sibu.common.util.HttpUtil;

public class FormLoginFilter extends FormAuthenticationFilter{
	private String loginUrl = "/login.jsp";  
    private String successUrl = "/";  
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject,
            ServletRequest request, ServletResponse response) throws Exception {
    	HttpUtil.writeSuccess(response, "登陆成功!");
	return false;
	}
    private boolean redirectToSuccessUrl(HttpServletRequest req, HttpServletResponse resp) throws IOException {  
        WebUtils.redirectToSavedRequest(req, resp, successUrl);  
        return false;  
    }  
    private void saveRequestAndRedirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {  
        WebUtils.saveRequest(req);  
        WebUtils.issueRedirect(req, resp, loginUrl);  
    }  
  
    private boolean login(HttpServletRequest req) {  
        String username = req.getParameter("username");  
        String password = req.getParameter("password");  
        try {  
            SecurityUtils.getSubject().login(new UsernamePasswordToken(username, password));  
        } catch (Exception e) {  
            req.setAttribute("shiroLoginFailure", e.getClass());  
            return false;  
        }  
        return true;  
    }  
    private boolean isLoginRequest(HttpServletRequest req) {  
        return pathsMatch(loginUrl, WebUtils.getPathWithinApplication(req));  
    }  
}
