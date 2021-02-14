package com.winter.taospring.web;

import com.winter.taospring.web.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 处理URL和Controller的method方法的映射，及相关功能完善
 */
public class HandlerMapping {

    private Pattern urlPattern;     //URL或URL的正则
    private Method method;          //对应要调用的方法
    private Object controller;      //对应的Controller
    private Class<?> [] paramTypes; //形参类型数组

    public HandlerMapping(Pattern pattern, Object controller, Method method) {
        this.urlPattern = pattern;
        this.method = method;
        this.controller = controller;
        paramTypes = method.getParameterTypes();
    }

    public Pattern getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(Pattern urlPattern) {
        this.urlPattern = urlPattern;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }
}
