package com.winter.taospring.aop.intercept;

import javax.transaction.TransactionRequiredException;

/**
 * 拦截器组件的统一接口
 */
public interface MethodInterceptor {

    Object invoke(MethodInvocation invocation) throws Throwable;
}
