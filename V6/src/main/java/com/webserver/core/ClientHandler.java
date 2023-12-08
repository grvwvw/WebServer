package com.webserver.core;

import com.webserver.http.HttpServletRequest;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 该线程任务负责与指定客户端完成HTTP交互请求
 */
public class ClientHandler implements Runnable{
    private Socket socket;
    public ClientHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            //1 解析请求
            HttpServletRequest request = new HttpServletRequest(socket);

            //2 处理请求

            //3 发送响应
            /**
             * 定位要发送的文件(将src/main/resources/static/myweb/index.html)
             *
             *  定义为resources目录(maven项目中的src/main/java和src/main/resources实际上是一个目录)
             *  只不过java目录中存放的都是.java源代码文件
             *  而resources存放的都是其他程序所需要用到的文件
             *
             *  实际开发过程中使用的相对路径是类加载路径
             *  类加载路径: 类名.class.getClassLoader().getResource(".")就是类加载路径
             *
             *  这里可以理解为是src/main/java和src/main/resources，是指编译后都位于target目录中
             */
            File root = new File(
                    ClientHandler.class.getClassLoader().getResource(".").toURI()
            ); //定位到了resources目录

            /*
                root表达的是src/main/java或者src/main/resources目录
                从root开始寻找static目录
             */
            File staticDir = new File(root, "static");

            /*
                在static目录下寻找index.html文件
             */
            File file = new File(staticDir, "myweb/index.html");

            System.out.println("资源是否存在" + file.exists());

            /**
             * 响应大致内容：
             * HTTP/1.1 200 OK(CRLF)
             * Content-Type: text/html(CRLF)
             * Content-Length: 2546(CRLF)(CRLF)
             * 1011101011101011011...
             */
            //3.1 发送请求行
            println("HTTP/1.1 200 OK");

            //3.2 发送响应头
            println("Content-Type: text/html");
            println("Content-Length: " + file.length());
            println(""); // 单独再发送一个CRLF

            //3.3 发送响应正文(index.html页面中的数据)
            OutputStream out = socket.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            int len;
            byte[] data = new byte[1024 * 10];
            while((len = fis.read(data)) != -1){
                out.write(data, 0, len); //使用块读块写的方法
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            //HTTP协议要求，响应完客户端之后需要断开TCP连接
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 将一个字符串按照ISO_8859_1编码发送给客户端，最后自动添加回车和换行符
     * @param line
     */
    private void println(String line) throws IOException {
        OutputStream out = socket.getOutputStream();
        byte[] data = line.getBytes(StandardCharsets.ISO_8859_1);
        out.write(data);
        out.write(13); //发送回车符
        out.write(10); // 发送换行符
    }


}
