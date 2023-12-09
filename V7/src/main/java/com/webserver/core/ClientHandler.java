package com.webserver.core;

import com.webserver.http.HttpServletRequest;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
            /**
             * http://localhost:8088/myweb/index.html
             * http://localhost:8088/myweb/classTable.html
             * http://localhost:8088/myweb/123.html //用户的不存在资源
             * 通过请求对象，获取浏览器地址栏中的抽象路径，然后在服务端(此处是本地)查找
             */
            String path = request.getUri();


            //3 发送响应
            File root = new File(ClientHandler.class.getClassLoader().getResource(".").toURI()); //定位到了resources目录
            File staticDir = new File(root, "static");
            File file = new File(staticDir, path);

            System.out.println("资源是否存在" + file.exists());

            int statusCode; // 状态代码
            String statusReason; //状态描述
            if(file.isFile()){ // 当file表示的文件真实存在，并且是一个真实的文件时才会返回true，不使用.exists()是因为会有不输入path路径的情况，相当于只查看路径是否存在
                statusCode = 200;
                statusReason = "OK";
            }else{ // 要么file表示的时一个目录，要么不存在
                statusCode = 404;
                statusReason = "NotFound";
                file = new File(staticDir, "root/404.html"); //将原本的文件路径修改到root/404.html文件处
            }

            //3.1 发送请求行
            println("HTTP/1.1 " + statusCode + " " + statusReason);

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
