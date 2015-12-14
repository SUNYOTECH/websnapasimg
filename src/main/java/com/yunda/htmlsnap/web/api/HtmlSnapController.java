/*******************************************************************************
 * Copyright (c) 2005, 2014 springside.github.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *******************************************************************************/
package com.yunda.htmlsnap.web.api;

import io.leopard.javahost.JavaHost;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.yunda.htmlsnap.service.BrowserFactory;
import com.yunda.htmlsnap.service.impl.BrowserService;

@Controller
@RequestMapping(value = "/snaphtml")
public class HtmlSnapController {
	private static Logger logger = LoggerFactory.getLogger(HtmlSnapController.class);
	@RequestMapping(method = RequestMethod.GET)
	public void snap(@RequestParam("htmlUrl") String htmlUrl,@RequestParam("imgType") String imgType,
			HttpServletRequest request, HttpServletResponse response) {
		logger.info("快照地址:"+ htmlUrl);
		//JavaHost.printAllVirtualDns();
		response.setContentType("image/"+imgType);
		response.setHeader("Content-Type","image/"+imgType);
		boolean flag = false;
		ServletOutputStream sos = null;
		if (StringUtils.isNotBlank(htmlUrl)) {
			BrowserService browserService = null;
			try {
				browserService = BrowserFactory.getBrowerService();
				sos = response.getOutputStream();
				flag = browserService.generatePng(URLDecoder.decode(htmlUrl, "UTF-8"),sos,imgType);
			} catch (IOException e) {
				logger.error("获取输出流异常:"+ e);
			}finally{
				BrowserFactory.recycleService(browserService);
			}
		}
		logger.info("生成图片结果："+flag);
		if (!flag) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		if (null !=sos) {
			try {
				sos.flush();
				sos.close();
			} catch (IOException e1) {
				logger.error("关闭流异常",e1);
			}
		}
	}
	
	 public void loadDns(){  
		 Resource resource = new ClassPathResource("/vdns.properties");  
        Properties props = null;
		try {
			props = PropertiesLoaderUtils.loadProperties(resource);
			JavaHost.updateVirtualDns(props);
			logger.info("成功加载虚拟DNS!");
		} catch (IOException e) {
			logger.error("加载虚拟DNS:"+ e);
		}  
	 }  

}
