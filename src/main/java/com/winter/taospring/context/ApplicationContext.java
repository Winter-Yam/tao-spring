package com.winter.taospring.context;

import com.winter.taospring.aop.AopProxy;
import com.winter.taospring.aop.JdkDynamicAopProxy;
import com.winter.taospring.aop.config.AopConfig;
import com.winter.taospring.aop.support.AdvisedSupport;
import com.winter.taospring.bean.*;
import com.winter.taospring.web.annotation.Autowired;
import com.winter.taospring.web.annotation.Controller;
import com.winter.taospring.web.annotation.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 应用上下文
 */
public class ApplicationContext extends DefaultListableBeanFactory implements BeanFactory {

    private String[] configLocations;
    private BeanDefinitionReader reader;

    // 通用IOC容器，存的是 BeanWrapper
    private Map<String, BeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();
    // 单例的IOC容器，存的是实例对象（相当于缓存，避免重复创建Bean）
    private Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<>();

    // 注入配置路径，初始化应用上下文
    public ApplicationContext(String ... configLocations) {
        this.configLocations = configLocations;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * IOC初始化入口
     * @throws Exception
     */
    @Override
    protected void refresh() throws Exception {
        // 1.定位，加载配置文件
        reader = new BeanDefinitionReader(configLocations);

        // 2.加载Bean对象
        List<BeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        // 3.注册配置信息到容器里面（伪IOC容器）
        doRegisterBeanDefinition(beanDefinitions);

        // 4.依赖注入
        doAutowired();
    }

    /**
     * 将Bean定义类的List转为Map
     * @param beanDefinitions
     */
    private void doRegisterBeanDefinition(List<BeanDefinition> beanDefinitions) throws Exception {
        for (BeanDefinition beanDefinition : beanDefinitions) {
            if (super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The “" + beanDefinition.getFactoryBeanName() + "” is exists!!");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }
    }

    /**
     * 完成所有Bean注入非延迟的依赖
     */
    private void doAutowired() {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if(!beanDefinition.isLazyInit()){
                try {
                    getBean(beanName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取Bean，或者初始化Bean并完成初始化
     * 1.将BeanDefinition转换为Bean，并包装为BeanWrapper放入到IOC容器
     * 2.对IOC容器中的Bean进行依赖注入
     */
    @Override
    public Object getBean(String beanName) throws Exception {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        Object instance = null;

        // 这里不应该直接创建
        BeanPostProcessor processor = new BeanPostProcessor();
        // 前置处理
        processor.postProcessBefore(instance, beanName);

        if(beanDefinition==null){
            throw new Exception("This Bean not exists!");
        }

        // 初始化，创建Bean对象
        BeanWrapper beanWrapper = instantiteBean(beanName, beanDefinition);

        // 2.将拿到的BeanWrapper放入IOC容器
        factoryBeanInstanceCache.put(beanName, beanWrapper);

        // 处理接口注入问题
        Class<?>[] interfaces = beanWrapper.getWrappedInstance().getClass().getInterfaces();
        for (Class<?> anInterface : interfaces) {
            this.factoryBeanInstanceCache.put(anInterface.getName(), beanWrapper);
            this.factoryBeanInstanceCache.put(anInterface.getName(), beanWrapper);
        }

        // 后置处理
        processor.postProcessAfter(instance, beanName);

        // 依赖注入，可以解决循环依赖
        populateBean(beanName, new BeanDefinition(), beanWrapper);

        // BeanWrapper的IOC容器中获取并返回Bean
        Object obj = this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();
        return obj;
    }

    /**
     * 只处理@Autowired注入
     */
    private void populateBean(String beanName, BeanDefinition beanDefinition, BeanWrapper beanWrapper) {
        Class<?> clazz = beanWrapper.getWrappedClass();
        // 只有容器管理的bean才会给他依赖注入
        if (! clazz.isAnnotationPresent(Controller.class ) || clazz.isAnnotationPresent(Service.class)) { return; }

        Object instance = beanWrapper.getWrappedInstance();
        // 注：这里是getDeclaredFields，getFields只能获取到public字段
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(Autowired.class)) {
                continue;
            }

            // 1.获取注解中指定注入的beanName（byName注入）
            Autowired annotation = field.getAnnotation(Autowired.class);
            String autowiredBeanName = annotation.value().trim();

            // 2.没有指定beanName的话，通过类型进行注入（byType注入）
            // 注意：类型 = 自身类型 || 接口类型。在BeanDefinitionReader#loadBeanDefinitions已经对接口创建过BeanDefinition了（当一个接口有多个实现类时，后扫描的会覆盖先扫描的）
            if ("".equals(autowiredBeanName)) {
                // 除了simpleName，通过class拿到的都是全类名
                // 前面初始化时已经用className向容器中注入过wrapper了
                autowiredBeanName = field.getType().getName();
            }

            field.setAccessible(true);

            try {
                // 因为要给当前Bean注入时，可能要注入的Bean还没初始化，因此就暂时不给这个字段注入
                // 但是当正式使用时还会getBean一次，这时所有bean都初始化完成了，就可以注入了
                if(this.factoryBeanInstanceCache.get(autowiredBeanName) == null){
                    continue;
                }

                // 获取具体Bean实例：这里是在通用IOC容器中获取，因为可能有多例情况
                field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private BeanWrapper instantiteBean(String beanName, BeanDefinition beanDefinition) {
        // 获取类名
        String className = beanDefinition.getBeanClassName();
        Object instance = null;
        // 默认为单例，判断单例容器中是否存在，否则通过反射创建
        try {
            if(factoryBeanObjectCache.containsKey(className)){
                instance = factoryBeanObjectCache.get(className);
            }else{
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();


                // 单例容器存放bean名和全限定名的实例
                this.factoryBeanObjectCache.put(beanDefinition.getFactoryBeanName(), instance);
                // =====================AOP入口========================
                // 读取配置文件中的信息，并构建一个AdvisedSupport对象
                AdvisedSupport config = initAopConfig(beanDefinition);
                config.setTargetClass(clazz);
                config.setTarget(instance);

                // 判断当前BeanDefinition的类是否符合PointCut的规则的话（即符合被代理的对象要求），创建将代理对象
                // 然后用代理对象替换当前对象，并放入IOC容器，
                // 到时 mvc 初始化IOC容器时，就会将代理对象放入，再后来创建处理请求的handler时就会将该代理对象封装进去
                if(config.pointCutMatch()) {
                    // 这时获取到的 Proxy 持有的AdvisedSupport已经构造好了拦截器链
                    // 到时 mvc 分发请求过来直接 proceed 执行即可
                    instance = createProxy(config).getProxy();
                }
                // =====================AOP结束========================
                this.factoryBeanObjectCache.put(className, instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 返回封装的BeanWrapper
        return new BeanWrapper(instance);
    }

    private AopProxy createProxy(AdvisedSupport config) {
        Class<?> targetClass = config.getTargetClass();
        // 当前类存在接口时，使用jdk的动态代理
        if (targetClass.getInterfaces().length > 0) {
            return new JdkDynamicAopProxy(config);
        }
        return null;
    }

    /**
     * 读取配置到AOPconfig，并转换为AdvisedSupport
     * @param beanDefinition
     * @return
     */
    private AdvisedSupport initAopConfig(BeanDefinition beanDefinition) {
        AopConfig config = new AopConfig();
        config.setPointCut(this.reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(this.reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(this.reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(this.reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(this.reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(this.reader.getConfig().getProperty("aspectAfterThrowingName"));

        return new AdvisedSupport(config);
    }

    @Override
    public Object getBean(Class<?> beanClass) throws Exception {
        // 同上一个方法
        return null;
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }
}
