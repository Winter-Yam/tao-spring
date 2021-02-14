package com.winter.demo.action;


import com.winter.demo.service.IModifyService;
import com.winter.demo.service.IQueryService;
import com.winter.taospring.web.ModelAndView;
import com.winter.taospring.web.annotation.Autowired;
import com.winter.taospring.web.annotation.Controller;
import com.winter.taospring.web.annotation.RequestMapping;
import com.winter.taospring.web.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 */
@Controller
@RequestMapping("/web")
public class MyAction {

	@Autowired
	IQueryService queryService;
	@Autowired
	IModifyService modifyService;

	@RequestMapping("/first.html")
    public ModelAndView query(@RequestParam("name") String name) {
        String result = queryService.query(name);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("name", name);
        model.put("data", result);
        model.put("token", 123456);
        return new ModelAndView("first", model);
    }

	@RequestMapping("/query.json")
	public ModelAndView query(HttpServletRequest request, HttpServletResponse response,
							  @RequestParam("name") String name){
		String result = queryService.query(name);
		return out(response,result);
	}

	@RequestMapping("/add*.json")
	public ModelAndView add(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam("name") String name, @RequestParam("addr") String addr){
		String result = null;
		try {
			// 该方法会抛出自定义异常
			result = modifyService.add(name,addr);
			return out(response,result);
		} catch (Exception e) {
			// 将异常信息保存在Map中，然后放入Model
			Map<String,Object> model = new HashMap<String,Object>();
			// 注：这里在单独测 mvc 模块时要去掉getCause
			model.put("detail",e.getMessage());
			model.put("stackTrace", Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]",""));

			return new ModelAndView("500",model);
		}

	}

	@RequestMapping("/remove.json")
	public ModelAndView remove(HttpServletRequest request, HttpServletResponse response,
                                 @RequestParam("id") Integer id){
		String result = modifyService.remove(id);
		return out(response,result);
	}

	@RequestMapping("/edit.json")
	public ModelAndView edit(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam("id") Integer id,
                               @RequestParam("name") String name){
		String result = modifyService.edit(id,name);
		return out(response,result);
	}



	private ModelAndView out(HttpServletResponse resp, String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void test(String name) {
        System.out.println(queryService.query(name));
    }

}
