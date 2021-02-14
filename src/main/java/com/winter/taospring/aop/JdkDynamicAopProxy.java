package com.winter.taospring.aop;

import com.winter.taospring.aop.intercept.MethodInvocation;
import com.winter.taospring.aop.support.AdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {

    private AdvisedSupport advise;

    public JdkDynamicAopProxy(AdvisedSupport advise) {
        this.advise = advise;
    }

    /**
     * 代理对象每个方法都会执行invoke
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 获取方法的拦截器链
        List<Object> interceptors = this.advise.getInterceptor(method, this.advise.getTargetClass());
        // 获取拦截器链的执行器
        MethodInvocation invocation = new MethodInvocation(proxy, this.advise.getTargetClass(), this.advise.getTarget(), method, args, interceptors);
        return invocation.proceed();
    }

    /**
     * 对外返回Proxy
     * @return
     */
    @Override
    public Object getProxy() {
        return getProxy(this.advise.getTargetClass().getClassLoader());
    }

    /**
     * 通过JDK动态代理生成具体的代理对象
     * @param classLoader
     * @return
     */
    @Override
    public Object getProxy(ClassLoader classLoader) {
        // Proxy.newProxyInstance会根据被代理对象的ClassLoader，class对象，和增强逻辑生成一个代理对象
        Object proxy = Proxy.newProxyInstance(classLoader, this.advise.getTargetClass().getInterfaces(), this);
        return proxy;
    }
}
