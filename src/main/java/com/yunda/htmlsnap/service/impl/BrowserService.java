package com.yunda.htmlsnap.service.impl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import chrriis.dj.nativeswing.swtimpl.NativeComponent;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;

@Service("browserService")
public class BrowserService {
	private static Logger logger = LoggerFactory.getLogger(BrowserService.class);
	// 行分隔符
	final static public String LS = System.getProperty("line.separator", "\n");
	// 文件分割符
	final static public String FS = System.getProperty("file.separator", "\\");
	//以javascript脚本获得网页全屏后大小
	private static StringBuffer jsDimension;
	private int num;
	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}
	private JWebBrowser webBrowser=null;
	
	public BrowserService() {
		try {
			init();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private int maxWidth=1280;
	private int maxHeight=953;
	private OutputStream ops = null;
	private final Lock lock = new ReentrantLock(); 
	private final Condition finishCondition = lock.newCondition();
	private String formatName = "png";
	private boolean hasreturn = false;
	
	static  {
		jsDimension = new StringBuffer();
		jsDimension.append("var width = 1280;").append(LS);
		jsDimension.append("var height = 953;").append(LS);
		jsDimension.append("if(document.documentElement) {").append(LS);
		jsDimension.append( "  width = Math.max(width, document.documentElement.scrollWidth);")
				.append(LS);
		jsDimension.append( "  height = Math.max(height, document.documentElement.scrollHeight);")
				.append(LS);
		jsDimension.append("}").append(LS);
		jsDimension.append("if(self.innerWidth) {").append(LS);
		jsDimension.append("  width = Math.max(width, self.innerWidth);")
				.append(LS);
		jsDimension.append("  height = Math.max(height, self.innerHeight);")
				.append(LS);
		jsDimension.append("}").append(LS);
		jsDimension.append("if(document.body.scrollWidth) {").append(LS);
		jsDimension.append( "  width = Math.max(width, document.body.scrollWidth);")
				.append(LS);
		jsDimension.append( "  height = Math.max(height, document.body.scrollHeight);")
				.append(LS);
		jsDimension.append("}").append(LS);
		jsDimension.append("return width + ':' + height;");
	}
	
	public boolean generatePng(final String url,OutputStream stream,final String imgType) {
		if (null == webBrowser) {
			try {
				init();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
//		try {
			//iops = ImageIO.createImageOutputStream(stream);
			 ops = stream;
//        } catch (IOException e) {
//        	logger.error("Can't create output stream! 不能创建！");
//        }
		formatName = imgType;
		boolean reault = false;
		hasreturn = false;
		logger.info("准备加载："+url);
		try {
			SwingUtilities.invokeAndWait(
					new Runnable() {
						public void run() {
							logger.info("加载："+url);
							webBrowser.stopLoading();
			webBrowser.navigate(url);
			}});
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		logger.info("阻塞等待结果");
		lock.lock();
		try {
			reault = finishCondition.await(60, TimeUnit.SECONDS);
			//reault = finishCondition.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			logger.info("结果返回");
			lock.unlock();
		}
		
		return reault;
	}

	private void init() throws InvocationTargetException, InterruptedException{
		logger.info("初始化");
		NativeInterface.open();
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
			
				// SWT组件转Swing组件，不初始化父窗体将无法启动webBrowser
				JFrame frame = new JFrame("以DJ组件保存指定网页截图");
				logger.info("初始化webBrowser");
				webBrowser = new JWebBrowser(null);
//				fileName = System.currentTimeMillis() + ".png";
				//webBrowser.navigate("http://localhost:8081/htmlsnapshot");
				webBrowser.setBarsVisible(false);
				webBrowser.setButtonBarVisible(false);
				webBrowser.setJavascriptEnabled(true);
				webBrowser.setStatusBarVisible(false);
				JComponent.setDefaultLocale(Locale.CHINA);
				webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
					// 监听加载进度
					
					@Override
					public void loadingProgressChanged(WebBrowserEvent e) {
						// 当加载完毕时
						logger.info("监听加载进度:"+e.getWebBrowser().getLoadingProgress());
						if (e.getWebBrowser().getLoadingProgress() == 100) {
							logger.info("加载完毕");
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
								logger.error("加载完毕等1秒失败");
							}
							if(hasreturn){
								logger.info("第二次加载完毕");
								return ;
							}
							logger.info("status:"+e.getWebBrowser().getStatusText());
							String result = (String) e.getWebBrowser().executeJavascriptWithResult(jsDimension.toString());
							logger.info("截图宽高:"+result);
							int index = result == null ? -1 : result.indexOf(":");
							NativeComponent nativeComponent = webBrowser.getNativeComponent();
							Dimension originalSize = nativeComponent.getSize();
							int h,w;
							if (result==null) {
								w=maxWidth;
								h=maxHeight;
							}else {
								w=Integer.parseInt(result .substring(0, index));
								h=Integer.parseInt(result .substring(index + 1));
							}
							Dimension imageSize = new Dimension(w,h );
							imageSize.width = Math.max(originalSize.width, imageSize.width + 10);
							imageSize.height = Math.max(originalSize.height, imageSize.height + 10);
							nativeComponent.setSize(imageSize);
							BufferedImage image = new BufferedImage(imageSize.width, imageSize.height, BufferedImage.TYPE_INT_RGB);
							nativeComponent.paintComponent(image);
							nativeComponent.setSize(originalSize);
							
							try {
								// 输出图像
								//ImageIO.write(image, "png", new File("D://tmp/random.png"));
								//ImageIO.write(image, "png", new File("D://tmp/random2.png"));
								//image.flush();
								//logger.info("ops:"+ops.toString());
								ByteArrayOutputStream os = new ByteArrayOutputStream();  
								ImageIO.write(image, formatName, os);
								logger.info("ops:"+ ops);
								if (null != ops) {
									logger.info("ops lengt:"+ os.toByteArray().length);
									ops.write(os.toByteArray());
								}
								lock.lock();
								finishCondition.signalAll();
								hasreturn=true;
							} catch (IOException ex) {
								ex.printStackTrace();
								logger.error("生成异常",ex);
							}finally{
								lock.unlock();
								
//								if (null !=ops) {
//									try {
//										ops.flush();
//										ops.close();
//									} catch (IOException e1) {
//										logger.error("关闭流异常",e1);
//									}
//								}
								ops = null;
							}
							logger.info("监听结束");
							// 退出操作
							//System.exit(0);
						}
					}

					@Override
					public void locationChanging(WebBrowserNavigationEvent e) {
						logger.info("locationChanging");
						super.locationChanging(e);
					}

					@Override
					public void locationChanged(WebBrowserNavigationEvent e) {
						logger.info("locationChanged");
						super.locationChanged(e);
					}
				});
				JPanel webBrowserPanel = new JPanel(new BorderLayout());
				webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
				// 加载google，最大保存为640x480的截图
				frame.getContentPane().add(webBrowserPanel, BorderLayout.CENTER);
				frame.setSize(800, 600);
				// 仅初始化，但不显示
				frame.invalidate();
				frame.pack();
				frame.setVisible(false);
				logger.info("初始化结束");
			}
		});
		//NativeInterface.runEventPump();
	}

}
