package com.sibu.common.mybatis.search;

/**
 * 扩展作为通用功能的查询实体
 * remark by zhangz
 * @author douk
 *
 */
public class SearchTerm {
	
	/* 分页查询数据属性 */
	private int page;
	private int size;
	private String sortName;
	private String sortOrder;
	/**
	 * 
	 */
	public SearchTerm() {
		super();
	}

	/**
	 * @param page
	 * @param size
	 */
	public SearchTerm(int page, int size) {
		super();
		
		if(page < 1)
			page = 1;
		
		if(size < 0)
			size = 0;
		
		this.page = page;
		this.size = size;
	}
	
	/**
	 * @param page
	 * @param size
	 * @param sortName
	 * @param sortOrder
	 */
	public SearchTerm(int page, int size, String sortName, String sortOrder) {
		super();
		
		if(page < 1)
			page = 1;
		
		if(size < 0)
			size = 0;
		
		this.page = page;
		this.size = size;
		this.sortName = sortName;
		this.sortOrder = sortOrder;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getSortName() {
		return sortName;
	}

	public void setSortName(String sortName) {
		this.sortName = sortName;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	
}
