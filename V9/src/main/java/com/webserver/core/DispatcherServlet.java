package com.webserver.core;

import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.File;
import java.net.URISyntaxException;

/**
 * 用于处理请求
 */
public class DispatcherServlet {
    private static File root;
    private static File staticDir;

    static {
        try {
            root = new File(ClientHandler.class.getClassLoader().getResource(".").toURI()); //定位到了resources目录
            staticDir = new File(root, "static");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws URISyntaxException {
        String path = request.getUri();

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
    }
}
