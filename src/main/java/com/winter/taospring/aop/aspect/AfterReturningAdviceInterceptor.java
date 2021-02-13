package com.winter.taospring.aop.aspect;

import com.winter.taospring.aop.intercept.MethodInterceptor;
import com.winter.taospring.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * 返回通知类
 */
public class AfterReturningAdviceInterceptor extends AbstractAspectAdvice implements Advise, MethodInterceptor {

    private JoinPoint joinPoint;

    public AfterReturningAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    private void afterReturning(Object retVal, Method method,Object[] args,Object target) throws Throwable{
        // 传入织入的参数，调用
        super.invokeAdviceMethod(this.joinPoint, retVal, null);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object retVal = invocation.proceed();
        this.joinPoint = invocation;
        afterReturning(retVal, invocation.getMethod(), invocation.getArguments(), invocation.getThis());
        return retVal;
    }
}
