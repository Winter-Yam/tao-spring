package com.winter.taospring.demo;


import com.winter.taospring.web.annotation.Autowired;
import com.winter.taospring.web.annotation.Controller;
import com.winter.taospring.web.annotation.RequestMapping;
import com.winter.taospring.web.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller增删查改基础演示
 */
@Controller
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private IDemoService demoService;

    @RequestMapping("/query")
    public void query(HttpServletRequest req, HttpServletResponse resp,
                      @RequestParam("name") String name, @RequestParam("age")String age){
        String result = demoService.get(name);
        // String result = "My name is " + name;
        try {
            resp.getWriter().write(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/add")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @RequestParam("a") Integer a, @RequestParam("b") Integer b){
        try {
            resp.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/sub")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @RequestParam("a") Double a, @RequestParam("b") Double b){
        try {
            resp.getWriter().write(a + "-" + b + "=" + (a - b));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/remove")
    public String  remove(@RequestParam("id") Integer id){
        return "" + id;
    }
}
