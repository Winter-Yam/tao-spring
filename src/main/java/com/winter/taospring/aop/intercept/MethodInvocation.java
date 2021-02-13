package com.winter.taospring.aop.intercept;

import com.winter.taospring.aop.aspect.JoinPoint;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 拦截器链的执行类
 */
public class MethodInvocation implements JoinPoint {

    private Object proxy;
    private Class<?> targetClass;
    private Object target;
    private Method method;
    private Object[] arguments;
    private List<Object> intercepters;
    // 记录当前拦截器的位置
    private int currentIntercepterIndex = -1;

    private Map<String, Object> userAttributes;

    public MethodInvocation(Object proxy, Class<?> targetClass, Object target, Method method,
                            Object[] arguments, List<Object> intercepters) {
        this.proxy = proxy;
        this.targetClass = targetClass;
        this.target = target;
        this.method = method;
        this.arguments = arguments;
        this.intercepters = intercepters;
    }

    /**
     * 执行拦截器链
     */
    public Object proceed() throws Throwable{
        // 如果拦截器都执行完，则执行joinPoint，即具体的方法
        if(currentIntercepterIndex==intercepters.size()-1){
            return method.invoke(target, arguments);
        }

        // 获取下一个拦截器
        Object interceptor = this.intercepters.get(++this.currentIntercepterIndex);

        // 判断是否为AOP拦截器
        if(interceptor instanceof MethodInterceptor) {
            MethodInterceptor methodInterceptor = (MethodInterceptor) interceptor;
            return methodInterceptor.invoke(this);
        }else{
            // 否则调用下一个拦截器
            return proceed();
        }
    }

    @Override
    public Object getThis() {
        return null;
    }

    @Override
    public Object[] getArguments() {
        return new Object[0];
    }

    @Override
    public Method getMethod() {
        return null;
    }

    @Override
    public void setUserAttribute(String key, Object value) {
        if (value != null) {
            if (this.userAttributes == null) {
                this.userAttributes = new HashMap<>();
            }
            this.userAttributes.put(key, value);
        }
        else {
            if (this.userAttributes != null) {
                this.userAttributes.remove(key);
            }
        }
    }

    @Override
    public Object getUserAttribute(String key) {
        return (this.userAttributes != null ? this.userAttributes.get(key) : null);
    }
}
