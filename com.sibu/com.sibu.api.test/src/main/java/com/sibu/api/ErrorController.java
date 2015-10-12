package com.sibu.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sibu.common.massage.ResponseUtil;
import com.sibu.common.util.Version;


@Controller
@RequestMapping(value = "/error",produces="application/json")
public class ErrorController {
	/**
	 * 
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	public String error(HttpServletRequest req, HttpServletResponse res) throws Exception {
		return ResponseUtil.error("非法链接");
	}
}
