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
 * 处理URL和method方法的映射，及相关功能完善
 */
public class Handler {

    private Pattern urlPattern;     //URL或URL的正则
    private Method method;          //对应要调用的方法
    private Object controller;      //对应的Controller
    private Class<?> [] paramTypes; //形参类型数组

    //形参列表
    //参数的名字作为key,参数的顺序，位置作为值（只处理@RequestParam修饰的）
    private Map<String,Integer> paramIndexMapping;

    public Handler(Pattern pattern, Object controller, Method method) {
        this.urlPattern = pattern;
        this.method = method;
        this.controller = controller;
        paramTypes = method.getParameterTypes();
        paramIndexMapping = new HashMap<String, Integer>();

        putParamIndexMapping(method);
    }

    // 填充形参列表
    private void putParamIndexMapping(Method method) {
        Annotation[][] annotationArray = method.getParameterAnnotations();
        int i = 0;
        //提取方法中的request和response参数
        Class<?> [] paramsTypes = method.getParameterTypes();
        for (i = 0; i < paramsTypes.length ; i ++) {
            Class<?> type = paramsTypes[i];
            if(type == HttpServletRequest.class ||
                    type == HttpServletResponse.class){
                paramIndexMapping.put(type.getName(),i);
            }
        }
        for (i = 0; i < annotationArray.length; i++) {
            Annotation[] annotations = annotationArray[i];
            for (Annotation annotation : annotations) {
                if(annotation instanceof RequestParam){
                    RequestParam requestParam = (RequestParam)annotation;
                    String paramName = requestParam.value();
                    if(!"".equals(paramName.trim())){
                        paramIndexMapping.put(paramName,i);
                    }
                }
            }
        }
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

    public Map<String, Integer> getParamIndexMapping() {
        return paramIndexMapping;
    }

    public void setParamIndexMapping(Map<String, Integer> paramIndexMapping) {
        this.paramIndexMapping = paramIndexMapping;
    }
}
