package com.winter.taospring.aop.aspect;

import java.lang.reflect.Method;

/**
 * 封装被切方法的信息
 */
public interface JoinPoint {

    Object getThis();

    Object[] getArguments();

    Method getMethod();

    void setUserAttribute(String key, Object value);

    Object getUserAttribute(String key);
}
