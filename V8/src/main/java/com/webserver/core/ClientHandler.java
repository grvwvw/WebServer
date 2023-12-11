package com.webserver.core;

import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

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
            HttpServletResponse response = new HttpServletResponse(socket);

            //2 处理请求
            String path = request.getUri();
            File root = new File(ClientHandler.class.getClassLoader().getResource(".").toURI()); //定位到了resources目录
            File staticDir = new File(root, "static");
            File file = new File(staticDir, path);

            System.out.println("资源是否存在" + file.exists());

            if(file.isFile()){ // 当file表示的文件真实存在，并且是一个真实的文件时才会返回true，不使用.exists()是因为会有不输入path路径的情况，相当于只查看路径是否存在
                response.setContentFile(file);
            }else{ // 要么file表示的时一个目录，要么不存在
                response.setStatusCode(404);
                response.setStatusReason("NotFound");

                file = new File(staticDir, "root/404.html"); //将原本的文件路径修改到root/404.html文件处
                response.setContentFile(file); // 设置对应的404文件位置
            }

            // 3. 发送响应
            response.response();



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




}
