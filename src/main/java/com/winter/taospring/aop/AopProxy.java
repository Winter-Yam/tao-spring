package com.winter.taospring.aop;


public interface AopProxy {

    Object getProxy();

    Object getProxy(ClassLoader classLoader);
}
