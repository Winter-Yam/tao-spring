package com.winter.taospring.bean;

/**
 * IOC容器中不是存原始的BeanDefinition，而是包装过的
 */
public class BeanWrapper {

    private Object wrappedInstance;

    private Class<?> wrappedClass;

    public BeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    public Object getWrappedInstance() {
        return wrappedInstance;
    }

    public Class<?> getWrappedClass() {
        return this.wrappedInstance.getClass();
    }
}
