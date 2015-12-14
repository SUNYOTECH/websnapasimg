#网页生成图片快照#
使用的核心开源项目：

|包名|版本|
|--|--|
|DJNativeSwing|1.0.3|
|DJNativeSwing-SWT|1.0.3|
|org.eclipse.swt.win32.win32.x86_64|4.3|

![](http://i.imgur.com/c6WnHai.png)
##调用
####请求地址
测试环境：http://IP:PORT/htmlsnapshot/snaphtml

调用方式 `GET`
 
|参数名|说明|
|----|----|
|htmlUrl|需要将response的html进行截图的请求地址，需要URLEncoder.encode(htmlUrl,"UTF-8")|
|imgType|生成图片的类型 如：PNG,GIF,JPG,BMP,RAF|
 
##保存返回的图片流
 
````java
HttpClient client = new HttpClient();
HttpMethod get = new GetMethod("http://localhost:8081/htmlsnapshot/snaphtml?htmlUrl=http%3A%2F%2Fsoa2.yundasys.com%3A8080%2Fydmobile%2Fbpsfinance%2Fzhengzhaosy%2F15346handler57090mbtmp.shtml&imgType=png");
int status = -1 ;
try {
    status = client.executeMethod(get);
    if (200 == status) {
        File file = new File(path);//保存图片至本地的路径
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
         fos = new FileOutputStream(file);
         in = get.getResponseBodyAsStream();//获取流
         out = new BufferedOutputStream(fos);
        int i;
        while((i=in.read())!=-1){
            out.write(i);
        }
        out.flush();
        out.close();
        in.close();
        System.out.println("生成文件："+path);
    }
} catch (Exception e) {
    ;
}finally{//释放资源
    get.releaseConnection();
    try {
    if (null!=out) {
            out.close();
    }
    if (null!=in) {
        in.close();
    }
    } catch (IOException e) {
        e.printStackTrace();
    }
}
````