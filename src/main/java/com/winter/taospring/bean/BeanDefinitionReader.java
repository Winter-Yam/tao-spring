package com.winter.taospring.bean;

import com.winter.taospring.web.annotation.Controller;
import com.winter.taospring.web.annotation.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 加载类似于xml的配置文件
 * 组装BeanDefinition
 */
public class BeanDefinitionReader {

    // 所有Bean的全限定名
    private List<String> registerBeanClassNames = new ArrayList<>();

    private Properties config = new Properties();

    public BeanDefinitionReader(String ... locations){
        // 读取web.xml配置文件application.properties位置
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));

        try {
            // 获取application.properties的属性到上下文中
            config.load(in);
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
        doScanner(config.getProperty("scanPackage"));
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
                    registerBeanClassNames.add(clazzName);
                }
            }
        }
    }

    /**
     * 类名转BeanDefinition
     */
    public List<BeanDefinition> loadBeanDefinitions(){
        List<BeanDefinition> result = new ArrayList<BeanDefinition>();
        try {
            for (String className : registerBeanClassNames) {
                // 加载或查找指定的类型
                Class<?> clazz = Class.forName(className);
                // 接口不能实例化，不处理
                if (clazz.isInterface()) {
                    continue;
                }

                // 组装Bean对象
                BeanDefinition beanDefinition = doCreateBeanDefinition(toLowerFirstCase(clazz.getSimpleName()), clazz.getName());
                result.add(beanDefinition);

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    // 再封装了构建 BeanDefinition 的方法
    private BeanDefinition doCreateBeanDefinition(String factoryBeanName, String beanClassName) {
        BeanDefinition myBeanDefinition = new BeanDefinition();
        myBeanDefinition.setBeanClassName(beanClassName);
        myBeanDefinition.setFactoryBeanName(factoryBeanName);
        return myBeanDefinition;
    }
    // 将首字母转为小写
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public Properties getConfig() {
        return config;
    }
}
