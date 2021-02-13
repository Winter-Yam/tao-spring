package com.winter.taospring.aop.support;

import com.winter.taospring.aop.aspect.AfterReturningAdviceInterceptor;
import com.winter.taospring.aop.aspect.AfterThrowingAdviceInterceptor;
import com.winter.taospring.aop.aspect.MethodBeforeAdviceInterceptor;
import com.winter.taospring.aop.config.AopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析AOP配置的类，建立切入方法与拦截器链的映射关系
 */
public class AdvisedSupport {
    // 目标类
    private Class<?> targetClass;
    // 目标对象，即被代理对象
    private Object target;
    // 配置文件信息
    private AopConfig config;
    // 被代理类特征正则表达式
    private Pattern pointCutClassPattern;
    // 切入点方法和拦截器链的映射关系
    private transient Map<Method, List<Object>> methodCache = new HashMap<>();

    private void parse(){
        String pointCut = config.getPointCut()
                .replaceAll("\\.","\\\\.")
                .replaceAll("\\\\.\\*",".*")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");

        // 1.通过正则表达式获取被代理类特征，初始化pointCutForClassRegex
        String pointCutForClassRegex = pointCut.substring(0,pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegex.substring(
                pointCutForClassRegex.lastIndexOf(" ") + 1));

        try {
            // 2.获取被切的类
            Class aspectClass = Class.forName(this.config.getAspectClass());
            // 保存切面类的所有方法
            Map<String,Method> aspectMethods = new HashMap<>();
            for (Method method : aspectClass.getMethods()) {
                aspectMethods.put(method.getName(), method);
            }

            // 获取切入点正则的Pattern
            Pattern pattern = Pattern.compile(pointCut);

            // 遍历当前类的所有方法
            for (Method method : this.targetClass.getMethods()) {
                String methodString = method.toString();
                if (methodString.contains("throws")) {
                    methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
                }

                // 判断当前方法是否是切点方法
                Matcher matcher = pattern.matcher(methodString);
                if(matcher.matches()){
                    // 若是，则构造一个执行器链，
                    List<Object> advices = new LinkedList<>();
                    // 把每一个方法包装成 MethodIterceptor
                    // before
                    if(config.getAspectBefore()==null||config.getAspectBefore().equals("")){
                        MethodBeforeAdviceInterceptor interceptor = new MethodBeforeAdviceInterceptor(aspectMethods.get(config.getAspectBefore()), aspectClass.newInstance());
                        advices.add(interceptor);
                    }
                    // after
                    if(config.getAspectAfter()==null||config.getAspectAfter().equals("")){
                        AfterReturningAdviceInterceptor interceptor = new AfterReturningAdviceInterceptor(aspectMethods.get(config.getAspectAfter()), aspectClass.newInstance());
                        advices.add(interceptor);
                    }
                    // throwing
                    if(config.getAspectAfterThrow()==null||config.getAspectAfterThrow().equals("")){
                        AfterThrowingAdviceInterceptor interceptor = new AfterThrowingAdviceInterceptor(aspectMethods.get(config.getAspectAfterThrow()), aspectClass.newInstance());
                        interceptor.setThrowingName(config.getAspectAfterThrowingName());
                        advices.add(interceptor);
                    }
                    // 将方法和拦截器链存入map中
                    methodCache.put(method, advices);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean pointCutMatch() {
        // 通过被代理类特征正则表达式来匹配当前类
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    /**
     * 获取指定方法的拦截器链
     */
    public List<Object> getInterceptor(Method method, Class<?> targetClass) throws Throwable{
        List<Object> advices = methodCache.get(method);
        if(advices==null){
            // 如果没有拦截器链，则缓存原方法
            Method originMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
            advices = methodCache.get(originMethod);
            // 对代理方法兼容处理
            methodCache.put(originMethod, advices);
        }
        return advices;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public AopConfig getConfig() {
        return config;
    }

    public void setConfig(AopConfig config) {
        this.config = config;
    }

    public Pattern getPointCutClassPattern() {
        return pointCutClassPattern;
    }

    public void setPointCutClassPattern(Pattern pointCutClassPattern) {
        this.pointCutClassPattern = pointCutClassPattern;
    }

    public Map<Method, List<Object>> getMethodCache() {
        return methodCache;
    }

    public void setMethodCache(Map<Method, List<Object>> methodCache) {
        this.methodCache = methodCache;
    }
}
