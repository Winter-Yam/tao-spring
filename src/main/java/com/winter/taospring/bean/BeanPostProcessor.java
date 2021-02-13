package com.winter.taospring.bean;

/**
 * 用于对Bean实例化前后进行自定义处理
 */
public class BeanPostProcessor {

    public Object postProcessBefore(Object bean, String beanName){
        return null;
    }

    public Object postProcessAfter(Object bean, String beanName){
        return null;
    }
}
