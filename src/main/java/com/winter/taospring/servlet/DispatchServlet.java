package com.winter.taospring.servlet;


import com.winter.taospring.annotation.Controller;
import com.winter.taospring.annotation.RequestMapping;
import com.winter.taospring.annotation.Service;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

// 暂时用@WebServlet模拟web.xml
@WebServlet(name="tao",urlPatterns="/*",initParams = {@WebInitParam(name="contextConfigLocation",value = "application.properties")})
public class DispatchServlet extends HttpServlet {

    // 模拟Spring的容器
    private Map<String,Object> mapping = new HashMap<>();

    /**
     * 用于初始化所有的类、IOC容器、servletBean
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        InputStream in = null;
        Properties configContext = new Properties();
        try {
            // 读取web.xml配置文件application.properties位置
            in = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));

            // 获取application.properties的属性
            configContext.load(in);
            String scanPackage = configContext.getProperty("scanPackage");

            // 扫描指定包目录
            doScanner(scanPackage);

            for (Map.Entry<String, Object> entry : mapping.entrySet()) {
                String className = entry.getKey();
                // 不带"."说明不是完整全限定名，不需要处理
                if (!className.contains(".")) {
                    continue;
                }
                // 加载或查找指定的类型
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(Controller.class)) {
                    // 根据@Controller注解进行处理，不需要将Controller计入容器，这里假设不需要使用
                    mapping.put(className,clazz.newInstance());
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
                        String url = (baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                        // 缓存url和controller方法的关系
                        mapping.put(url, method);
                        System.out.println("Mapped " + url + "," + method);
                    }
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    // 根据@Service注解进行处理
                    Service service = clazz.getAnnotation(Service.class);
                    // 配置bean name
                    String beanName = service.value();
                    if ("".equals(beanName)) {
                        beanName = clazz.getName();
                    }
                    // 创建bean的实例
                    Object instance = clazz.newInstance();
                    // 将实现类加入容器中
                    mapping.put(beanName, instance);

                    // 将实现类对应的接口类也加入到容器中，用于面向接口编程
                    for (Class<?> i : clazz.getInterfaces()) {
                        mapping.put(i.getName(), instance);
                    }

                } else {
                    continue;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            this.destroy();
            if(in != null){
                try {
                    in.close();
                } catch (Exception e) {
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
                    mapping.put(clazzName,null);
                }
            }
        }
    }
}
