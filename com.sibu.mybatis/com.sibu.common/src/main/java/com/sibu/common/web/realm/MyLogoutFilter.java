package com.sibu.common.web.realm;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.util.WebUtils;

import com.sibu.common.massage.ResponseUtil;
import com.sibu.common.util.HttpUtil;

public class MyLogoutFilter extends LogoutFilter {
		
	/**
     * Issues an HTTP redirect to the specified URL after subject logout.  This implementation simply calls
     * {@code WebUtils.}{@link WebUtils#issueRedirect(javax.servlet.ServletRequest, javax.servlet.ServletResponse, String) issueRedirect(request,response,redirectUrl)}.
     *
     * @param request  the incoming Servlet request
     * @param response the outgoing Servlet response
     * @param redirectUrl the URL to where the browser will be redirected immediately after Subject logout.
     * @throws Exception if there is any error.
     */
    protected void issueRedirect(ServletRequest request, ServletResponse response, String redirectUrl) throws Exception {
//        WebUtils.issueRedirect(request, response, redirectUrl);
    	HttpUtil.writeSuccess(response,"退出成功!");
    }
}
