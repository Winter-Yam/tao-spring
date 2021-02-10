package com.winter.taospring.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class JdkDynamicAopProxy implements InvocationHandler {

    private AdvisedSupport config;

    public JdkDynamicAopProxy(AdvisedSupport config) {
        this.config = config;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
