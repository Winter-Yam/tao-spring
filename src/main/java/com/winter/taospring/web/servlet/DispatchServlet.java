package com.winter.taospring.web.servlet;


import com.winter.taospring.context.ApplicationContext;
import com.winter.taospring.web.*;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 暂时用@WebServlet模拟web.xml
@WebServlet(name="tao",urlPatterns="/*",initParams = {@WebInitParam(name="contextConfigLocation",value = "application.properties")})
public class DispatchServlet extends HttpServlet {
    private Properties configContext = new Properties();
    private List<String> classNames = new ArrayList<>();

    // Spring的IOC容器
    private ApplicationContext context;

    //保存url和Method的对应关系
    private List<HandlerMapping> handlerMappings = new ArrayList<>();
    // 保存视图解析器的容器
    private List<ViewResolver> viewResolvers = new ArrayList<>();
    // 保存 <HandlerMapping，HandlerAdpter> 映射关系的容器（用于获取执行方法的请求适配器）
    private Map<HandlerMapping, HandlerAdapter> handereAdpters = new HashMap<>();

    /**
     * 用于初始化所有的类、IOC容器、servletBean
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1.初始化ApplicationContext ！！！
        // tomcat会加载web.xml并创建其中配置的servlet，同时会执行init方法，这里的config即web.xml配置信息
        context = new ApplicationContext(config.getInitParameter("contextConfigLocation"));

        // 2.初始化SpringMVC九大组件
        initStrategies();
    }

    /**
     * 部分组件暂不实现
     */
    private void initStrategies() {
        // 多文件上传的组件
        //initMultipartResolver(context);
        // 初始化本地语言环境
        //initLocaleResolver(context);
        // 初始化模板处理器
        //initThemeResolver(context);


        // handlerMapping，必须实现
        initHandlerMapping();
        // 初始化参数适配器，必须实现
        initHandlerAdapters();
        // 初始化异常拦截器
        //initHandlerExceptionResolvers(context);
        // 初始化视图预处理器
        //initRequestToViewNameTranslator(context);


        // 初始化视图转换器，必须实现
        initViewResolvers();
        // 参数缓存器
        //initFlashMapManager(context);
    }

    private void initViewResolvers() {
        // 拿到在配置文件中配置的模板存放路径(layouts)
        String templateRoot = context.getConfig().getProperty("templateRoot");

        // 通过相对路径找到目标后，获取到绝对路径
        // 注：getResourse返回的是URL对象，getFile返回文件的绝对路径
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        // 拿到模板目录下的所有文件名（这里是所有html名）
        File templateRootDir = new File(templateRootPath);
        String[] templates = templateRootDir.list();

        // 视图解析器可以有多种，且不同的模板需要不同的Resolver去解析成不同的View（jsp，html，json。。）
        // 但这里其实就只有一种（解析成html）
        // 为了仿真才写了这个循环，其实只循环一次
        for (int i = 0; i < templates.length; i ++) {
            this.viewResolvers.add(new ViewResolver(templateRoot));
        }
    }

    private void initHandlerAdapters() {
        // 为每一个 HandlerMapping 都创建一个 HandlerAdpter
        for (HandlerMapping handlerMapping : this.handlerMappings) {
            this.handereAdpters.put(handlerMapping, new HandlerAdapter());
        }
    }

    private void initHandlerMapping() {
        try {
            for (String beanName : context.getBeanDefinitionNames()) {
                Object controller = context.getBean(beanName);
                Class<?> clazz = controller.getClass();
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
                        handlerMappings.add(new HandlerMapping(pattern, controller,method));
                        System.out.println("Mapped " + url + "," + method);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        // 获取HandlerMapping
        HandlerMapping handler = getHandler(req, resp);
        if(handler==null){
            processDispatchResult(req, resp, new ModelAndView("404"));
            return;
        }

        // 2.获取当前handler对应的处理参数的Adpter
        HandlerAdapter handlerAdapter = getHandlerAdapter(handler);

        // 3.Adpter负责处理 request 中携带的参数然后执行处理请求的方法
        // 执行的结果可能是null(增加、删除、异常...）也可能是ModelAndView（查询...）
        // Adpter真正调用处理请求的方法,返回ModelAndView（存储了页面上值，和页面模板的名称）
        ModelAndView mv = handlerAdapter.handle(req, resp, handler);

        // 4.真正输出,将方法执行进行处理然后返回
        // 如果上面返回的是 ModelAndView ，那么还要通过视图解析器和模板引擎进行解析
        processDispatchResult(req, resp, mv);
    }

    /**
     * 处理请求的结果，并解析成HTML
     */
    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, ModelAndView mv) throws Exception {
        // null 表示方法返回类型是void，或返回值是null。不做额外处理
        if(mv == null) {
            return;
        }

        // 如果没有视图解析器就返回，因为无法处理ModelAndView
        if (this.viewResolvers.isEmpty()) {
            return;
        }

        // 遍历视图解析器
        for (ViewResolver viewResolver : this.viewResolvers) {
            // 通过相应解析器，返回相应页面 View
            View view = viewResolver.resolveViewName(mv.getViewName(), null);
            // View通过模板引擎（自定义的）解析后输出
            view.render(mv.getModel(), req, resp);
            return;
        }
    }

    private HandlerMapping getHandler(HttpServletRequest req, HttpServletResponse resp){
        if (handlerMappings.isEmpty()) {
            return null;
        }
        // 获取请求的URL
        // 绝对路径
        String url = req.getRequestURI();
        // 处理成相对路径
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        for (HandlerMapping handler : handlerMappings) {
            // 具体的URL与handler的正则匹配
            Matcher matcher = handler.getUrlPattern().matcher(url);
            if(matcher.find()){
                return handler;
            }
        }
        return null;
    }

    private HandlerAdapter getHandlerAdapter(HandlerMapping handler) {
        if (this.handereAdpters.isEmpty()) {
            return null;
        }
        HandlerAdapter handlerAdpter = this.handereAdpters.get(handler);
        // 判断当前handler能否被当前Adapter进行适配
        if (handlerAdpter.supports(handler)) {
            return handlerAdpter;
        }
        return null;
    }
}
