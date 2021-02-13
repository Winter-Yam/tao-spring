package com.winter.taospring.aop.aspect;

import com.winter.taospring.aop.intercept.MethodInterceptor;
import com.winter.taospring.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * 前置通知类
 */
public class MethodBeforeAdviceInterceptor extends AbstractAspectAdvice implements Advise, MethodInterceptor {

    private JoinPoint joinPoint;

    public MethodBeforeAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    private void before(Method method,Object[] args,Object target) throws Throwable{
        // 传入织入的参数，调用
        super.invokeAdviceMethod(this.joinPoint, null, null);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        this.joinPoint = invocation;
        before(invocation.getMethod(), invocation.getArguments(), invocation.getThis());
        return invocation.proceed();
    }
}
