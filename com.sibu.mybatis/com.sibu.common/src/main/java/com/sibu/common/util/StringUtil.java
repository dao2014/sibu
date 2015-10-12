package com.sibu.common.util;



import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * String工具类
 * @version 1.0
 */
public class StringUtil {
	
	/**
	 * 随机产生0-99字符串
	 * @return
	 */
	public static String randomTwo(){
		Random random = new Random();
		int ran = random.nextInt(100);
		return String.valueOf(ran);
	}
	/**
	 * 判断一个字符串的首字母是不是大写
	 * @param s 被判断的字符串
	 * @return boolean 返回true表示是，返回false表示否
	 */
	public static boolean isCapitalize(String s){
		if(s.charAt(0)>='A'  &&  s.charAt(0)<='Z'){
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 判断一个对象是否是空，如果是空则返回true，否则返回false
	 * @param obj
	 * @return boolean
	 */
	public static boolean isNull(Object obj){
		if(null==obj){
			return true;
		} else if(obj instanceof String){
			if("".equals(((String)obj).trim()) || "null".equals(((String)obj).trim().toLowerCase()))
				return true;
			return false;
		} else if(obj instanceof List){
			if(((List<?>)obj).isEmpty())
				return true;
			return false;
		} else if(obj instanceof HashSet){
			if(((HashSet<?>)obj).isEmpty())
				return true;
			return false;
		} else if(obj instanceof HashMap){
			if(((HashMap<?,?>)obj).isEmpty())
				return true;
			return false;
		} else if(obj instanceof Set){
			if(((Set<?>)obj).isEmpty())
				return true;
			return false;
		} else {
			return false;
		}
	}
	/**
	 * 排除null字符串
	 * @data 2015年1月13日
	 * @version 1.0
	 * @param s 字符串
	 * @return String 排除了null的字符串
	 */
	public static String excludeNullStr(String s){
		if(null==s || "".equals(s.trim()) || "null".equals(s.trim().toLowerCase())){
			return null;
		} else {
			return s;
		}
	}
	/**
	 * 获取字符串
	 * @param obj 所有对象
	 * @return String 字符串
	 */
	public static String getString(Object obj){
		if(null!=obj)
			return obj.toString().trim();
		return "";
	}
	
	/**
	 * 获取整型
	 * @param obj
	 * @return int
	 */
	public static int getInt(Object obj){
		if(null==obj || "".equals(obj.toString().trim())){
			return 0;
		} else {
			return Integer.valueOf(obj.toString().trim());
		}
	}
	
	/**
	 * 获取浮点小数
	 * @param obj
	 * @return double
	 */
	public static double getDouble(Object obj){
		if(null==obj || "".equals(obj.toString().trim())){
			return 0d;
		} else {
			return Double.valueOf(obj.toString().trim()).doubleValue();
		}
	}
	
	/**
     * 判断字符串是否表示ture,如果是则返回true,如果不是则返回false
     * @param s 待判断字符串
     * @return boolean 结果
     */
    public static boolean isTrue(String s){
    	if(!isNull(s)){
    		if(("true".equals(s.trim().toLowerCase()))){
    			return true;
    		} else if(("false".equals(s.trim().toLowerCase()))){
    			return false;
    		} else {
    			try {
					throw new Exception("字符串：" + s + "，并不是boolean类型！" );
				} catch (Exception e) {
					e.printStackTrace();
				}
    		}
    	}
    	return false;
    }
    
    /**
	 * 获取长整型
	 * @param s 字符串
	 * @return Long
	 */
	public static Long getLong(String s){
		if(null!=s && !"".equals(s)){
			return Long.valueOf(s.trim());
		}
		return 0L;
	}
	
	/**
	 * 判断一个字符串是否存在一个数组当中
	 * @param str 字符串
	 * @param ss 数组
	 * @return boolean 返回true表示存在；返回false表示不存在
	 */
	public static boolean isExist(String str, String[] ss){
		boolean isExit = false;
		for(String s : ss){
			if(str.equals(s)){
				isExit = true;
				break;
			}
		}
		return isExit;
	}
	
	/**
     * double类型取小数点后面几位
     * @param val 指定double型数字
     * @param precision 取前几位
     * @return 转换后的double数字
     */
    public static Double roundDouble(double val, int precision) {   
        Double ret = null;   
        try {   
            double factor = Math.pow(10, precision);   
            ret = Math.floor(val * factor + 0.5) / factor;   
        } catch (Exception e) {   
            e.printStackTrace();   
        }   
  
        return ret;   
    }
	/**
	 * 获得class的绝对路径
	 * @return String class的绝对路径
	 */
	public static String getClassRootPath(){
		String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		rootPath = rootPath.substring(1, rootPath.length()).replaceAll("%20", " ");
		return rootPath;
	}
    
	/**
	 * 将整数转换成字符串
	 * @param n 整数
	 * @param leng 指定字符串长度
	 * @return String 字符串
	 */
	public static String intToString(int n, int leng){
		String s = Integer.valueOf(n).toString();
		while(s.length()<leng){
			s = "0"+s;
		}
		return s;
	}
	/**
	 * 将空字符串变成null
	 * @param s
	 * @return String
	 */
	public static String setEmptyToNull(String s) {
		if("".equals(s)){
			return null;
		}else{
			return s;
		}
	}
	
	/**
	 * 将null变成空字符串
	 * @param s
	 * @return String
	 */
	public static String setNullToEmpty(String s) {
		if(s == null){
			return "";
		}else{
			return s;
		}
	}
	
	/**
	 * 去前后空格，排除null的字符串
	 * @param s 字符串
	 * @return String 字符串
	 */
	public static String trimNull(String s){
		if(null==s){
			return null;
		}
		return setEmptyToNull(s.trim());
	}
	/**
	 * 将数组转换成字符串
	 * @param obj
	 * @return String
	 */
	public static String ArrayToStr(char[] obj){
		StringBuffer sb = new StringBuffer();
		if(null!=obj && obj.length>0){
			for(int i=0;i<obj.length;i++){
				sb.append(String.valueOf(obj[i]));
			}
		}
		return sb.toString();
	}
	/**
	 * 验证是否输入数字
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str){
		  for (int i = str.length();--i>=0;){   
		   if (!Character.isDigit(str.charAt(i))){
		    return true;
		   }
		  }
		  return false;
		 }
	
	/**
	 * 获取UUID
	 * @return
	 */
	public static String getUUID(){
        
		return UUID.randomUUID().toString().toUpperCase(); 
	}
}
