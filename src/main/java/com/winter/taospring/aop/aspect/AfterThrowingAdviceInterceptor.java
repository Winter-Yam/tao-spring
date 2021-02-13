package com.winter.taospring.aop.aspect;

import com.winter.taospring.aop.intercept.MethodInterceptor;
import com.winter.taospring.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * 异常通知类
 */
public class AfterThrowingAdviceInterceptor extends AbstractAspectAdvice implements Advise, MethodInterceptor {

    private String throwingName;

    public AfterThrowingAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            Object retVal = invocation.proceed();
            return retVal;
        } catch (Throwable e) {
            invokeAdviceMethod(invocation, null, e.getCause());
            throw e;
        }
    }

    public void setThrowingName(String throwingName) {
        this.throwingName = throwingName;
    }
}
