package com.sibu.dao.user.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;

import com.sibu.dao.user.bean.UserBean;

/**
 * UserMapper
 * @author dao
**/
public interface UserMapper {
	/** 
	 * 设置关联的dao接口缓存，设置后，清除该接口的缓存数据时会同时清除关联的接口的数据缓存。
	 * 里面设置缓存dao接口的包名+类名，例如：correlationCache = {"com.trisun.parking.dao.SysUserMapper"}
	 * 提示：如无关联缓存设置，可以删除该字段
	 **/
	public String[] correlationCache = null;
	/**
	 * 查询统计
	 * @param brean 对象
	 * @return int 总记录数
	 * @throws Exception
	 */
	int count(UserBean brean) throws Exception;
	/**
	 * 获取数据列表
	 * @param params 参数集
	 * @return rowBounds  添加分页条件
	 * @throws Exception
	 */
	List<UserBean> list(RowBounds rowBounds, Map<String, Object> params);
	/**
	 * 获取数据列表
	 * @param params 参数集
	 * @throws Exception
	 */
	List<UserBean> list( Map<String, Object> params);
	/**
	 * 保存
	 * @param bean bean对象
	 * @return Integer ID
	 * @throws Exception
	 */
	Integer save(Object bean) throws Exception;
	/**
	 * 获取对象数据
	 * @param params 参数集
	 * @return List<SysUserBean> 记录集
	 * @throws Exception
	 */
	UserBean get(String id) throws Exception;
	/**
	 * 更新
	 * @param params 参数集
	 * @return boolean 是否操作成功
	 * @throws Exception
	 */
	Integer update(Map<String, Object> params) throws Exception;
	/**
	 * 删除
	 * @param id 数据id
	 * @return boolean 是否操作成功
	 * @throws Exception
	 */
	int delete(String id) throws Exception;
	/**
	 * 批量删除
	 * @param ids 数据ID集合
	 * @return boolean 是否操作成功
	 * @throws Exception
	 */
	boolean batchDelete(List<?> list) throws Exception;
}
