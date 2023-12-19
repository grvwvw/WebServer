package com.webserver.controller;

import com.webserver.core.DispatcherServlet;
import com.webserver.entity.User;
import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.*;
import java.net.URISyntaxException;

/**
 * 处理用户相关业务
 */
public class UserController {
    private static File userDir;
    private static File root;
    private static File staticDir;

    static {
        try {
            root = new File(DispatcherServlet.class.getClassLoader().getResource(".").toURI()); //定位到了resources目录
            staticDir = new File(root, "static");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        userDir = new File("./users");
        if(!userDir.exists()){
            userDir.mkdirs();
        }
    }
    public void reg(HttpServletRequest request, HttpServletResponse response){
        //1. 获取注册页面上输入的注册信息，获取form表单提交的内容
        /*
            getParameter传入的值必须是和表单页面上对应的输入框名字一致
            即:<input name="username" type="text">
                            ^^^^^^^^
        */
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String nickname = request.getParameter("nickname");
        String ageStr = request.getParameter("age");
        int age = Integer.parseInt(ageStr);

        System.out.println(username + " " + password + " " + nickname + " " + ageStr);
        //2. 将用户信息保存
        File userFile = new File(userDir, username + ".obj");
        try(
            FileOutputStream fos = new FileOutputStream(userFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
        ){
          User user = new User(username, password, nickname, age);
          oos.writeObject(user);

          File file = new File(staticDir, "/myweb/reg_success.html");
          response.setContentFile(file);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //3. 给用户响应一个注册结果页面(注册成功或者失败)
    }
}
