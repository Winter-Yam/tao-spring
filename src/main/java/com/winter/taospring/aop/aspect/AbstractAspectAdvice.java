package com.winter.taospring.aop.aspect;

import java.lang.reflect.Method;

/**
 * 切面通知抽象类
 */
public class AbstractAspectAdvice implements Advise {

    // 被切方法
    private Method aspectMethod;
    // 被切对象
    private Object aspectTarget;

    public AbstractAspectAdvice(Method aspectMethod, Object aspectTarget) {
        this.aspectMethod = aspectMethod;
        this.aspectTarget = aspectTarget;
    }

    public Object invokeAdviceMethod(JoinPoint joinPoint, Object returnValue, Throwable throwable) throws Throwable{
        Class<?>[] parameterTypes = aspectMethod.getParameterTypes();
        if(parameterTypes==null||parameterTypes.length==0){
            // 方法无参数，直接调用
            Object invoke = aspectMethod.invoke(aspectTarget);
            return invoke;
        }else{
            // 有形参
            Object[] args = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                if(parameterTypes[i] == JoinPoint.class){
                    args[i] = joinPoint;
                }else if(parameterTypes[i] == Throwable.class){
                    args[i] = throwable;
                }else if(parameterTypes[i] == Object.class){
                    args[i] = returnValue;
                }
            }
            return this.aspectMethod.invoke(aspectTarget,args);
        }
    }
}
