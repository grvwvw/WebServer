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
            DispatcherServlet servlet = new DispatcherServlet();
            servlet.service(request, response);

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
