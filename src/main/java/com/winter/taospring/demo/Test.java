package com.winter.taospring.demo;


import com.winter.taospring.context.ApplicationContext;

public class Test {

    public static void main(String[] args) {
        // 初始化 IOC 容器
        ApplicationContext context = new ApplicationContext("classpath:application.properties");
        try {
            // 测试 IOC
            Object bean = context.getBean("myAction");
            System.out.println(bean);
            // 测试 DI，即 myAction 对象中是否成功注入 QueryService 对象
            MyAction myAction = (MyAction)bean;
            myAction.test("張三");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

