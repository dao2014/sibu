package com.sibu.common.mybatis.page;

import org.springframework.util.StringUtils;


/**
 * 处理分页参数
 *
 */
public class ParamUtil {
	
	/**
	 * 解析页码参数，无效则返回1
	 * 
	 * @param page
	 * @return
	 */
	public static int getPage(String page){
		int _page = 0;
		try {
			_page = Integer.parseInt(page);
		} catch (Exception e) {
			
		}
		
		if(_page == 0 || _page < 0){
			_page = 1;
		}
		
		return _page;
	}
	
	/**
	 * 解析页码大小参数，无效返回默认大小
	 * 
	 * @param size
	 * @return
	 */
	public static int getPageSize(String size){
		
		int _size = 0;
		try {
			_size = Integer.parseInt(size);
		} catch (Exception e) {
			
		}
		
		if(_size == 0 || _size < 0){
			_size = Constants.psgeSize;
		}
			
		return _size;
	}
	
	/**
	 * 获取排序字段名称
	 * 
	 * @param sortName
	 * @param defaulVal
	 * @return
	 */
	public static String getSortName(String sortName, String defaulVal){
		
		if(StringUtils.isEmpty(sortName)){
			
			return sortName;
		}
		
		return defaulVal;
	}
	
	/**
	 * 获取排序方式
	 * 
	 * @param sortName
	 * @param defaulVal
	 * @return
	 */
	public static String getSortOrder(String sortName, String defaulVal){
		
		if(StringUtils.isEmpty(sortName)){
			
			return sortName;
		}
		
		return defaulVal;
	}
}
