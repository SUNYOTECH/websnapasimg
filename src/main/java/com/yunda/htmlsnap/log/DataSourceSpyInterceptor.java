package com.yunda.htmlsnap.log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;

import net.sf.log4jdbc.log.SpyLogFactory;
import net.sf.log4jdbc.sql.jdbcapi.ConnectionSpy;
import net.sf.log4jdbc.sql.jdbcapi.DriverSpy;
import net.sf.log4jdbc.sql.rdbmsspecifics.RdbmsSpecifics;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 扩展的一个拦截器类,扩展主要是使用AOP的方式，因为log4jdbc原来的方式不适合此项目
 * @author songjie
 *
 */
public class DataSourceSpyInterceptor implements MethodInterceptor {
	
	private RdbmsSpecifics rdbmsSpecifics = null;  
    
	private static Method method = null;
	
    private RdbmsSpecifics getRdbmsSpecifics(Connection conn) {
        if(rdbmsSpecifics == null) {
    		try {
    			if (null ==  method) {
    				method = DriverSpy.class.getDeclaredMethod("getRdbmsSpecifics", Connection.class);
				}
    			method.setAccessible(true);
    			rdbmsSpecifics = (RdbmsSpecifics) method.invoke(null, conn);
    			method.setAccessible(false);
    		} catch (SecurityException e) {
    			e.printStackTrace();
    		} catch (NoSuchMethodException e) {
    			e.printStackTrace();
    		} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
        }
        return rdbmsSpecifics;
    }  
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		 Object result = invocation.proceed();  
	        if(SpyLogFactory.getSpyLogDelegator().isJdbcLoggingEnabled()) {  
	            if(result instanceof Connection) {
	                Connection conn = (Connection)result;  
	                return new ConnectionSpy(conn,getRdbmsSpecifics(conn),SpyLogFactory.getSpyLogDelegator());  
	            }
	        }
	        return result;
	}



}
