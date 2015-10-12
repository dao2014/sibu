package com.sibu.common.web.handler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import com.sibu.common.web.message.Message;
import com.sibu.common.web.message.MsgType;

/**
 * SpringMVC异常处理，页面请求返回view, 其他请求返回json
 * 
 */
public class CustomSimpleMappingExceptionResolver extends
		SimpleMappingExceptionResolver {

	@Override
	protected ModelAndView doResolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception e) {

		// Expose ModelAndView for chosen error view.
		String viewName = determineViewName(e, request);

		// Apply HTTP status code for error views, if specified.
		// Only apply it if we're processing a top-level request.
		if (viewName != null) {
			Integer statusCode = determineStatusCode(request, viewName);
			if (statusCode != null) {
				applyStatusCodeIfPossible(request, response, statusCode);
			}
			return getModelAndView(viewName, e, request);
		} else {
				response.setContentType("application/json; charset=utf-8");
			try {
				PrintWriter writer = response.getWriter();
				writer.write(new Message(MsgType.exception, e.getMessage())
						.toJson());
				writer.flush();
			} catch (IOException ioe) {
				e.printStackTrace();
			}
		}

		return null;
	}

}
