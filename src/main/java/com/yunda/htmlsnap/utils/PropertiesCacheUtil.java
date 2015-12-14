package com.yunda.htmlsnap.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 配置文件加载工具类
 *
 */
public class PropertiesCacheUtil {
	private static final Logger logger = LoggerFactory.getLogger(PropertiesCacheUtil.class);
	private static PropertiesCacheUtil instance = null;//单例模式实例
	private static String propertiesFile = "serviceConfigure.properties";//配置文件名称
	private static String propertiesFilePath= Thread.currentThread().getContextClassLoader().getResource(propertiesFile).getPath();//配置文件路径
	
	private static boolean checkFile = true;//true,每个打开此类时判断文件是否被修改，修改则重新加载 false,不判断	
	private static Map<String, Long> checkFileMap = new HashMap<String,Long>();	//用于验证文件是否被修改 
	private static Map<String, String> configMap = new HashMap<String, String> ();

	private PropertiesCacheUtil(){//私有构造函数
		setYdsoaConfigMap();
		setCheckFileMap();
		instance = this;
	}

	/**
	 * 单例 获取配置文件类PropertiesCacheUtil 的实例
	 * @return
	 */
	private static PropertiesCacheUtil getInstance(){
		if(checkFile){
			if((null == instance)|| null == checkFileMap.get(propertiesFile) || !(checkFileMap.get(propertiesFile).equals((new File(propertiesFilePath)).lastModified())))
			{//配置文件有改动重新加载
				logger.info("配置文件serviceConfigure有改动重新加载");
				return new PropertiesCacheUtil();
			}else{
				logger.debug("配置文件serviceConfigure无改动");
				return instance;
			}
		}
		else{
			if(instance == null)
				return new PropertiesCacheUtil();
			else
				return instance;
		}
	}
	/**
	 * 加载配置文件信息
	 */
	private void setYdsoaConfigMap(){
		Properties properties = new Properties();
		try{
			InputStream in  = new FileInputStream(new File(propertiesFilePath));
			properties.load(in);
			for(Entry<Object, Object> entry: properties.entrySet()){
				configMap.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
			}
 		}catch(IOException e){
 			e.printStackTrace();
 		}
	}
	
	/**
	 * 文件最后修改时间放入缓存
	 */
	private void setCheckFileMap() {
		checkFileMap.put(propertiesFile,new Long(new File(propertiesFilePath).lastModified()));
	}
	/**
	 * 对外提供接口,根据配置文件的keyName获取keyValue
	 * @param keyName
	 * @return
	 */
	public static String getKey(String keyName){
		getInstance();
		return configMap.get(keyName);
	}
	
	/**
	 * 对外提供接口,根据配置文件的keyName获取keyValue,如果没有值返回默认值
	 * @param keyName
	 * @param defaultValue
	 * @return
	 */
	public static String getKey(String keyName, String defaultValue) {
		
		String value = getKey(keyName);
		
		return value==null? defaultValue: value;
	}
	
	
	public static void main(String args[]){
		System.out.println(PropertiesCacheUtil.getKey("serviceNo"));
		System.out.println(PropertiesCacheUtil.getKey("serviceNo2","21"));
		System.out.println(System.currentTimeMillis());
	}
}
