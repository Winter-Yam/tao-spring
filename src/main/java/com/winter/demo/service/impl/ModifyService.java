package com.winter.demo.service.impl;

import com.winter.demo.service.IModifyService;
import com.winter.taospring.web.annotation.Service;


/**
 * 增删改业务
 */
@Service
public class ModifyService implements IModifyService {

	/**
	 * 增加
	 */
	public String add(String name,String addr) throws Exception {
		throw new Exception("这是故意抛的异常！！");
		//return "modifyService add,name=" + name + ",addr=" + addr;
	}

	/**
	 * 修改
	 */
	public String edit(Integer id,String name) {
		return "modifyService edit,id=" + id + ",name=" + name;
	}

	/**
	 * 删除
	 */
	public String remove(Integer id) {
		return "modifyService id=" + id;
	}
	
}