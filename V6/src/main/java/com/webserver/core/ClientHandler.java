package com.webserver.core;

import com.webserver.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
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
            File root = new File(ClientHandler.class.getClassLoader().getResource(".").toURI()); //定位到了resources目录

            /*
                root表达的是src/main/java或者src/main/resources目录
                从root开始寻找static目录
             */
            File staticDir = new File(root, "static");


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
