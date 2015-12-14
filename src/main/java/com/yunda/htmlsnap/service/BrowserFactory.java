package com.yunda.htmlsnap.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yunda.htmlsnap.service.impl.BrowserService;
import com.yunda.htmlsnap.utils.PropertiesCacheUtil;

public class BrowserFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(BrowserFactory.class);
	
	private static BlockingQueue<BrowserService> browserServiceQueque ;
	static int serviceNo=10;
	static {
		serviceNo = Integer.valueOf(PropertiesCacheUtil.getKey("serviceNo", "10"));
		init();
	}
	public static void init(){
		browserServiceQueque = new ArrayBlockingQueue<BrowserService>(serviceNo);
		initQueue();
	}
	
	private static void initQueue(){
		
		for (int i = 1; i <= serviceNo; i++) {
			BrowserService bs = new BrowserService();
					bs.setNum(i);
			browserServiceQueque.offer(bs);
			logger.info("初始编号："+bs.getNum()+"hashcode:"+bs);
		}
	}
	
	/**
	 * 获取指定类型的service
	 * @param docType
	 * @return
	 */
	public static BrowserService getBrowerService() {
		BrowserService bds = null;
		bds = browserServiceQueque.poll();//没资源等待等待
		logger.info("从  service Queue中获取编号："+bds.getNum()+"的service,,剩余service数："+browserServiceQueque.size());
		return bds;
	}
	
	/**
	 * 回收service
	 * @param baseDocService
	 */
	public static void recycleService(BrowserService browserService){

		browserServiceQueque.offer(browserService);
			logger.info("编号为："+browserService.getNum()+"的"+browserService.getClass().getSimpleName() +"被收回,剩余service数："+browserServiceQueque.size());
	}


}
