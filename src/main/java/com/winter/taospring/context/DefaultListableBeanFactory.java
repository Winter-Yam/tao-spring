package com.winter.taospring.context;

import com.winter.taospring.bean.BeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认IOC容器实现
 */
public class DefaultListableBeanFactory extends AbstractApplicationContext {

    // 类似IOC容器，但只保存了Bean的定义信息
    protected final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
}
