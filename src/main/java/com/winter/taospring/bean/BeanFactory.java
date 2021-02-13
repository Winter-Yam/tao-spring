package com.winter.taospring.bean;

/**
 * 获取Bean的工厂，IOC容器规范之一
 */
public interface BeanFactory {

    Object getBean(String beanName) throws Exception;

    Object getBean(Class<?> beanClass) throws Exception;
}
