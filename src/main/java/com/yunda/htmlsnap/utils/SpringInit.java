package com.yunda.htmlsnap.utils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SpringInit implements ServletContextListener {
    private static WebApplicationContext springContext;
    
    public SpringInit() {
        super();
    }
    
    public void contextInitialized(ServletContextEvent event) {
        springContext = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
        System.out.println("初始化springContext");
    }
    

    public void contextDestroyed(ServletContextEvent event) {
    }
    
    public static ApplicationContext getApplicationContext() {
        return springContext;
    }
    /**
     * 仅供 测试用 切勿调用
     * @param servletContext
     */
    public static void setApplicationContext(WebApplicationContext servletContext){
    	springContext = servletContext;
    }

    
}

