package com.winter.taospring.web;


import java.util.Map;

/**
 * 包含了要返回的页面，以及页面里所需要的参数
 */
public class ModelAndView {

    private String ViewName;

    private Map<String, ?> model;

    public ModelAndView(String viewName, Map<String, ?> model) {
        ViewName = viewName;
        this.model = model;
    }

    public ModelAndView(String viewName) {
        ViewName = viewName;
    }

    public String getViewName() {
        return ViewName;
    }

    public void setViewName(String viewName) {
        ViewName = viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }

    public void setModel(Map<String, ?> model) {
        this.model = model;
    }
}
