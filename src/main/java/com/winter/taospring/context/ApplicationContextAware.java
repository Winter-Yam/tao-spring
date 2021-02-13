package com.winter.taospring.context;

/**
 * 上下文感知，通过解耦方式注入ApplicationContext
 */
public interface ApplicationContextAware {

    void setApplicationContext(ApplicationContext applicationContext);
}
