package com.winter.taospring.web;

import com.winter.taospring.web.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 将Request转换为Controller可以处理的参数
 * 并与形成匹配后执行
 */
public class HandlerAdapter {

    //形参列表
    //参数的名字作为key,参数的顺序，位置作为值（只处理@RequestParam修饰的）
    private Map<String,Integer> paramIndexMapping = new HashMap<>();

    public boolean supports(Object handler){
        return (handler instanceof HandlerMapping);
    }


    public ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception{
        HandlerMapping handlerMapping = (HandlerMapping) handler;

        Method method = handlerMapping.getMethod();
        // 获取Controller方法顺序
        putParamIndexMapping(method);
        // 处理请求中的参数，并转换为方法可处理的形式
        Object[] paramValues = handleRequestParam(req, resp, method);

        // 反射调用方法
        Object returnValue = method.invoke(handlerMapping.getController(), paramValues);

        // 返回结果塞到resp中
        if (returnValue == null || returnValue instanceof Void) {
            return null;
        }
        // 如果该方法返回ModelAndView
        // 那么，还需要再多走一步，即对 ModelAndView进行解析
        boolean isModelAndView = handlerMapping.getMethod().getReturnType() == ModelAndView.class;
        if (isModelAndView) {
            // 注意，这里要将返回值强转为ModelAndView
            return (ModelAndView) returnValue;
        }
        return null;
    }

    private Object[] handleRequestParam(HttpServletRequest req, HttpServletResponse resp, Method method) {
        // 获取请求参数
        Map<String, String[]> parameterMap = req.getParameterMap();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        Object [] paramValues = new Object[parameterTypes.length];

        for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
            // 获取请求参数对应的值
            String value = Arrays.toString(param.getValue())
                    .replaceAll("\\[|\\]","");

            // 检查该请求参数是否有修改@RequestParam
            if (!paramIndexMapping.containsKey(param.getKey())) {
                continue;
            }
            // 设置到参数数组中
            Integer index = paramIndexMapping.get(param.getKey());
            paramValues[index] = convert(parameterTypes[index], value);
        }

        // 处理req和resp类型的参数
        if (paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }

        if (paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
            int respIndex = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = resp;
        }
        return paramValues;
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

    /**
     * 转换真实类型，方面后面的处理
     */
    //url传过来的参数都是String类型的，HTTP是基于字符串协议
    //只需要把String转换为任意类型就好
    private Object convert(Class<?> type, String value) {
        //如果是int
        if (Integer.class == type) {
            return Integer.valueOf(value);
        } else if (Double.class == type) {
            return Double.valueOf(value);
        }
        //如果还有double或者其他类型，继续加if
        //这时候，我们应该想到策略模式了
        //在这里暂时不实现，希望小伙伴自己来实现
        return value;
    }
}
