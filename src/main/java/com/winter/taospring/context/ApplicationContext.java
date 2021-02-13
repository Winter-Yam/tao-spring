package com.winter.taospring.context;

import com.winter.taospring.bean.*;

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

        // 后置处理
        processor.postProcessAfter(instance, beanName);

        // 依赖注入，可以解决循环依赖
        populateBean(beanName, new BeanDefinition(), beanWrapper);

        // BeanWrapper的IOC容器中获取并返回Bean
        Object obj = this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();
        return obj;
    }

    /**
     * 可以根据@Resource和@Autowired注入
     */
    private void populateBean(String beanName, BeanDefinition beanDefinition, BeanWrapper beanWrapper) {

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
                this.factoryBeanObjectCache.put(className, instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 返回封装的BeanWrapper
        return new BeanWrapper(instance);
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
