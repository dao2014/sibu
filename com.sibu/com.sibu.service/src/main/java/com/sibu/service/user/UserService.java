package com.sibu.service.user;

import java.util.List;
import java.util.Map;

import com.sibu.common.bean.StatusBean;
import com.sibu.dao.user.vo.UserBean;

/**
 * UserService
 * @author dao
**/
public interface UserService {
	/**
	 * 根据ID获取类别信息
	 * @param id 数据ID
	 * @return UserBean 数据bean对象
	 * @throws Exception
	 **/
	public UserBean getBeanById(Map<String, Object> params) throws Exception;
	
	/**
	 * 根据ID获取类别信息
	 * @param id 数据ID
	 * @return UserBean 数据bean对象
	 * @throws Exception
	 **/
	public UserBean get(Map<String, Object> params) throws Exception;
	/**
	 * 查询所有数据
	 * @return List<SysMenuExtendBean>
	 * @throws Exception
	 */
	public StatusBean<UserBean> login(Map<String, Object> params) throws Exception;
	
	/**
	 * 登陆
	 * @return List<SysMenuExtendBean>
	 * @throws Exception
	 */
	public List<UserBean> listAll() throws Exception;
	/**
	 * 查询记录总数
	 * @param params 查询条件
	 * @return int 查询的记录总数
	 * @throws Exception
	 **/
	public int count(UserBean brean) throws Exception;
	/**
	 * 查询所有数据
	 * @param params 查询条件
	 * @return List<UserBean> 查询后返回的结果集
	 * @throws Exception
	 **/
	public List<UserBean> list(Map<String, Object> params) throws Exception;
	/**
	 * 保存
	 * @param bean 需要保存的bean对象
	 * @param user 当前操作人
	 * @return StatusBean 操作状态
	 * @throws Exception
	 **/
	public StatusBean<?> save(UserBean bean) throws Exception;
	/**
	 * 更新
	 * @param bean 需要更新的bean对象
	 * @param user 当前操作人
	 * @return boolean 返回true表示更新成功；返回false表示更新失败； 
	 * @throws Exception
	 **/
	public StatusBean<?> update(UserBean bean) throws Exception;
	/**
	 * 删除
	 * @param id 待删除的数据ID
	 * @return boolean 返回true表示删除成功；返回false表示删除失败；
	 * @throws Exception
	 **/
	public StatusBean<?> deleteById(String id)  throws Exception;
	/**
	 * 批量删除
	 * @param ids 待删除的数据ID数组
	 * @return boolean 返回true表示删除成功；返回false表示删除失败；
	 * @throws Exception
	 **/
	public StatusBean<?> batchDelete(String ids) throws Exception;
}
