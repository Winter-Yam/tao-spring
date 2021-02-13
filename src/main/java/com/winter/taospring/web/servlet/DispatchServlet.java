package com.winter.taospring.web.servlet;


import com.winter.taospring.web.HandlerMapping;
import com.winter.taospring.web.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 暂时用@WebServlet模拟web.xml
@WebServlet(name="tao",urlPatterns="/*",initParams = {@WebInitParam(name="contextConfigLocation",value = "application.properties")})
public class DispatchServlet extends HttpServlet {
    private Properties configContext = new Properties();
    private List<String> classNames = new ArrayList<>();

    // Spring的IOC容器
    private Map<String,Object> ioc = new HashMap<>();

    //保存url和Method的对应关系
    private List<HandlerMapping> handlerMapping = new ArrayList<>();

    /**
     * 用于初始化所有的类、IOC容器、servletBean
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1.加载配置文件
        loadConfig(config.getInitParameter("contextConfigLocation"));
        // 2.扫描Bean的类
        doScanner(configContext.getProperty("scanPackage"));
        // 3.初始化并加入到IoC容器中
        doIoc();
        // 4.完成依赖注入
        doAutowire();
        // 5.初始化HandlerMapping
        initHandlerMapping();

        System.out.println("Spring framework init success");
    }

    private void initHandlerMapping() {
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (clazz.isAnnotationPresent(Controller.class)) {

                String baseUrl = "";
                if (clazz.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                    baseUrl = requestMapping.value();
                }
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(RequestMapping.class)) {
                        continue;
                    }
                    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                    String url = ("/"+baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                    // url转换为正则
                    Pattern pattern = Pattern.compile(url);
                    // 缓存url和controller方法的关系
                    handlerMapping.add(new HandlerMapping(pattern, entry.getValue(),method));
                    System.out.println("Mapped " + url + "," + method);
                }
            }
        }
    }

    private void doAutowire() {
        if(ioc.isEmpty()){
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            // 获取所有被@Autowired注解的字段
            Object instance = entry.getValue();
            Field[] fields = instance.getClass().getDeclaredFields();
            for (Field field : fields) {
                if(!field.isAnnotationPresent(Autowired.class)){
                    continue;
                }
                // 如果没有自定义Autowire名称，默认注入类型的名称
                Autowired autowired = field.getAnnotation(Autowired.class);
                String beanName = autowired.value().trim();
                if("".equals(beanName)){
                    //获得接口的类型，作为key待会拿这个key到ioc容器中去取值（接口需要获取全限定名）
                    beanName = field.getType().getName();
                }

                // 设置private可访问
                field.setAccessible(true);
                try {
                    // 模拟自动注入，用反射设置值
                    field.set(instance, ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doIoc() {
        try {
            for (String className : classNames) {
                // 不带"."说明不是完整全限定名，不需要处理
                if (!className.contains(".")) {
                    continue;
                }
                // 加载或查找指定的类型
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(Controller.class)) {
                    // 根据@Controller注解进行处理，不需要将Controller计入容器，这里假设不需要使用
                    //Spring默认类名首字母小写
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    // 根据@Service注解进行处理
                    Service service = clazz.getAnnotation(Service.class);
                    // 配置bean name
                    String beanName = service.value();
                    if ("".equals(beanName.trim())) {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }
                    // 创建bean的实例
                    Object instance = clazz.newInstance();
                    // 将实现类加入容器中
                    ioc.put(beanName, instance);

                    // 将实现类对应的接口类也加入到容器中，用于面向接口编程（实际逻辑会有点问题）
                    for (Class<?> i : clazz.getInterfaces()) {
                        if(ioc.containsKey(i.getName())){
                            System.out.println("The “" + i.getName() + "” is exists!!");
                        }
                        ioc.put(i.getName(), instance);
                    }

                } else {
                    continue;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadConfig(String contextConfigLocation) {
        // 读取web.xml配置文件application.properties位置
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);

        try {
            // 获取application.properties的属性到上下文中
            configContext.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != in){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doScanner(String scanPackage) {
        // 获取包的绝对目录地址
        // classloader.getResource用于加载绝对路径
        URL url = this.getClass().getClassLoader().getResource(scanPackage.replaceAll("\\.", "/"));
        // 根据URL创建class目录
        File classDir = new File(url.getFile());
        // 遍历目录下的所有文件
        for (File file : classDir.listFiles()) {
            if(file.isDirectory()){
                // 如果是目录，递归扫描
                doScanner(scanPackage+"."+file.getName());
            }else{
                // 如果是文件，判断是否为class文件
                if(file.getName().endsWith(".class")){
                    // 将class文件对应的全限定类名缓存到mapping中
                    String clazzName = (scanPackage + "." + file.getName().replace(".class",""));
                    classNames.add(clazzName);
                }
            }
        }
    }

    //为了简化程序逻辑，就不做其他判断了，大家了解就OK
    //其实用写注释的时间都能够把逻辑写完了
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        //之所以加，是因为大小写字母的ASCII码相差32，
        // 而且大写字母的ASCII码要小于小写字母的ASCII码
        //在Java中，对char做算学运算，实际上就是对ASCII码做算学运算
        chars[0] += 32;
        return String.valueOf(chars);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            // 异常返回500
            resp.getWriter().write("500 Exection,Detail : " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        HandlerMapping handler = getHandler(req, resp);
        if(handler==null){
            resp.getWriter().write("404 Not Found!!");
            return;
        }

        Method method = handler.getMethod();

        // 获取请求参数
        Map<String, String[]> parameterMap = req.getParameterMap();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        Object [] paramValues = new Object[parameterTypes.length];

        Map<String, Integer> paramIndexMapping = handler.getParamIndexMapping();
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

        // 反射调用方法
        Object returnValue = method.invoke(handler.getController(), paramValues);

        // 返回结果塞到resp中
        if (returnValue == null || returnValue instanceof Void) {
            return;
        }
        resp.getWriter().write(returnValue.toString());
    }

    private HandlerMapping getHandler(HttpServletRequest req, HttpServletResponse resp){
        if (handlerMapping.isEmpty()) {
            return null;
        }
        // 获取请求的URL
        // 绝对路径
        String url = req.getRequestURI();
        // 处理成相对路径
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        for (HandlerMapping handler : handlerMapping) {
            // 具体的URL与handler的正则匹配
            Matcher matcher = handler.getUrlPattern().matcher(url);
            if(matcher.find()){
                return handler;
            }
        }
        return null;
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
