/*******************************************************************************
 * $Header$
 * $Revision$
 * $Date$
 *
 *==============================================================================
 *
 * Copyright (c) 2001-2006 Primeton Technologies, Ltd.
 * All rights reserved.
 * 
 * Created on 2015-6-2
 *******************************************************************************/


package top.appcity.websnapasimg.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chrriis.dj.nativeswing.NSOption;
import chrriis.dj.nativeswing.swtimpl.NativeComponent;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;

public class HtmlCaptureUtil {
	private static Logger logger = LoggerFactory.getLogger(HtmlCaptureUtil.class);
	// 行分隔符
	final static public String LS = System.getProperty("line.separator", "\n");
	// 文件分割符
	final static public String FS = System.getProperty("file.separator", "\\");
	//以javascript脚本获得网页全屏后大小
	final static StringBuffer jsDimension;
	
	private static JWebBrowser webBrowser=null;
	private static int maxWidth=1280;
	private static int maxHeight=953;
	private static ImageOutputStream ops = null;
	private static final Lock lock = new ReentrantLock(); 
	private static final Condition finishCondition = lock.newCondition();
	private static String formatName = "png";
	
	static {
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
	public static synchronized boolean generatePng(final String url,OutputStream stream,final String imgType) {
		if (null == webBrowser) {
			init();
		}
		try {
			 ops = ImageIO.createImageOutputStream(stream);
        } catch (IOException e) {
        	logger.error("Can't create output stream! 不能创建！");
        }
		formatName = imgType;
		boolean reault = false;
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
		webBrowser.navigate(url);
		}});
		logger.info("阻塞等待结果");
		lock.lock();
		try {
			reault = finishCondition.await(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
		logger.info("结果返回");
		
		return reault;
	}

	private static void init(){
		NativeInterface.open();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			
				// SWT组件转Swing组件，不初始化父窗体将无法启动webBrowser
				JFrame frame = new JFrame("以DJ组件保存指定网页截图");
				logger.info("初始化webBrowser");
				webBrowser = new JWebBrowser(new NSOption(null));
//				fileName = System.currentTimeMillis() + ".png";
				//webBrowser.navigate("http://10.19.105.164:7030/eos-governor/governor/user/login.jsp");
				webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
					
					@Override
					public void statusChanged(WebBrowserEvent e) {
						super.statusChanged(e);
						System.out.println("jsenable:"+e.getWebBrowser().isJavascriptEnabled());
						System.out.println("stattext:"+e.getWebBrowser().getStatusText());
						String result = (String) webBrowser
								.executeJavascriptWithResult(jsDimension.toString());
						System.out.println("ss-result:"+result);
					}

					// 监听加载进度
					public void loadingProgressChanged(WebBrowserEvent e) {
						// 当加载完毕时
						logger.info("监听加载进度");
						if (e.getWebBrowser().getLoadingProgress() == 100) {
							logger.info("加载完毕");
							lock.lock();
//							fileName = System.currentTimeMillis() + ".png";
							String result = (String) webBrowser
									.executeJavascriptWithResult(jsDimension.toString());
							System.out.println("result:"+result);
							int index = result == null ? -1 : result.indexOf(":");
							NativeComponent nativeComponent = webBrowser
									.getNativeComponent();
							Dimension originalSize = nativeComponent.getSize();
							Dimension imageSize = new Dimension(Integer.parseInt(result
									.substring(0, index)), Integer.parseInt(result
									.substring(index + 1)));
							imageSize.width = Math.max(originalSize.width,
									imageSize.width + 50);
							imageSize.height = Math.max(originalSize.height,
									imageSize.height + 50);
							nativeComponent.setSize(imageSize);
							BufferedImage image = new BufferedImage(imageSize.width,
									imageSize.height, BufferedImage.TYPE_INT_RGB);
							nativeComponent.paintComponent(image);
							nativeComponent.setSize(originalSize);
							// 当网页超出目标大小时
							if (imageSize.width > maxWidth
									|| imageSize.height > maxHeight) {
								//截图部分图形
								image = image.getSubimage(0, 0, maxWidth, maxHeight);
								/*此部分为使用缩略图
								int width = image.getWidth(), height = image
									.getHeight();
								 AffineTransform tx = new AffineTransform();
								tx.scale((double) maxWidth / width, (double) maxHeight
										/ height);
								AffineTransformOp op = new AffineTransformOp(tx,
										AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
								//缩小
								image = op.filter(image, null);*/
							}
							try {
								// 输出图像
								ImageIO.write(image, "png", new File("D://tmp/random.png"));
								//logger.info("ops:"+ops.toString());
								ImageIO.write(image, formatName, ops);
								finishCondition.signalAll();
							} catch (IOException ex) {
								ex.printStackTrace();
								logger.error("生成异常",ex);
							}finally{
								lock.unlock();
								//ops = null;
								/*if (null !=ops) {
									try {
										ops.flush();
									} catch (IOException e1) {
										logger.error("关闭流异常",e1);
									}
								}*/
							}
							logger.info("结束");
							
							// 退出操作
							//System.exit(0);
						}
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
			}
		});
		//NativeInterface.runEventPump();
	}
}
