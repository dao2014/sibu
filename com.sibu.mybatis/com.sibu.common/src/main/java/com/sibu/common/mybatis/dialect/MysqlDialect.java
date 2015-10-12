package com.sibu.common.mybatis.dialect;

public class MysqlDialect {

	public String getLimitString(String queryString, int offset, int pageSize) {  
        StringBuffer buffer = new StringBuffer(queryString);  
        buffer.append(" limit ");  
        buffer.append(offset);  
        buffer.append(",");  
        buffer.append(pageSize);  
        return buffer.toString();  
    } 
}
