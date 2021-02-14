package com.winter.taospring.web;

import org.apache.tomcat.jni.Local;

import java.io.File;

/**
 * 负责解析ModelAndView的View路径，返回View对象
 */
public class ViewResolver {

    private final String DEFALUT_TEMPALTE_SUFIX = ".html";

    // 视图目录
    private File templateRootDir;

    public ViewResolver(String templateRoot) {
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        this.templateRootDir = new File(templateRootPath);
    }

    public View resolveViewName(String viewName, Local locale) throws Exception{
        if (null == viewName || "".equals(viewName.trim())) {
            return null;
        }
        // 给没有 .html的加上后缀（我们可以在ModelAndView中写500.html，也可以直接写 500）
        viewName = viewName.endsWith(DEFALUT_TEMPALTE_SUFIX) ? viewName : (viewName + DEFALUT_TEMPALTE_SUFIX);
        // 返回相应视图
        File templateFile = new File((templateRootDir.getPath() + "/" + viewName).replaceAll("/+", "/"));
        return new View(templateFile);

    }
}
