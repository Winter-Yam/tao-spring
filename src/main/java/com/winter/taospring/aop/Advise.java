package com.winter.taospring.aop;

import java.lang.reflect.Method;

/**
 * AOP通知定义
 */
public class Advise {

    private Object aspect;
    private Method method;
    private String throwName;

    public Advise(Object aspect, Method method, String throwName) {
        this.aspect = aspect;
        this.method = method;
        this.throwName = throwName;
    }

    public Object getAspect() {
        return aspect;
    }

    public void setAspect(Object aspect) {
        this.aspect = aspect;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getThrowName() {
        return throwName;
    }

    public void setThrowName(String throwName) {
        this.throwName = throwName;
    }
}
