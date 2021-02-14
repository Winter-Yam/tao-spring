package com.winter.demo.service.impl;

import com.winter.demo.service.IQueryService;
import com.winter.taospring.web.annotation.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 查询业务
 */
@Service
public class QueryService implements IQueryService {

	/**
	 * 查询
	 */
	public String query(String name) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = sdf.format(new Date());
		String json = "{name:\"" + name + "\",time:\"" + time + "\"}";
//		log.info("这是在业务方法中打印的：" + json);
		return json;
	}

}
