package com.webserver.controller;

import com.webserver.core.DispatcherServlet;
import com.webserver.entity.User;
import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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

        /**
         * 必要的验证，要求
         * 1. 四项内容不能为空，并且年龄一定要是数字(正则表达式)
         * 2. 否则返回一个注册失败的页面: reg_input_error.html
         * 该页面显示一行字: 输入信息有误，注册失败
         */
        if(username == null || password == null || nickname == null || ageStr == null || !ageStr.matches("[0-9]+")){
            File file = new File(staticDir, "/myweb/reg_input_error.html");
            response.setContentFile(file);
            return;
        }

        int age = Integer.parseInt(ageStr);

        System.out.println(username + " " + password + " " + nickname + " " + ageStr);
        //2. 将用户信息保存
        File userFile = new File(userDir, username + ".obj");

        /**
         * 判断是否为重复用户，若重复用户，则响应页面: have_user.html
         */
        if(userFile.exists()){ //说明当前是重复的用户
            File file = new File(staticDir, "/myweb/have_user.html");
            response.setContentFile(file);
            return;
        }

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

    public void login(HttpServletRequest request, HttpServletResponse response){
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if(username == null || password == null){
            File file = new File(staticDir, "/myweb/login_info_error.html");
            response.setContentFile(file);
            return;
        }

        File file = new File(userDir, username + ".obj");
        if(file.exists()){
            try (
                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(fis);
            ){
                User user = new User();
                user = (User) ois.readObject();
                if (username.equals(user.getUsername()) && password.equals(user.getPassword())){
                    File newFile = new File(staticDir, "/myweb/login_success.html");
                    response.setContentFile(newFile);
                }else{
                    File newFile = new File(staticDir, "/myweb/login_fail.html");
                    response.setContentFile(newFile);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 用于动态显示用户列表的动态页面
     * @param request
     * @param response
     */
    public void showAllUser(HttpServletRequest request, HttpServletResponse response){
        //1. 先用users目录中所有的用户读入进一个List集合中
        List<User> userList = new ArrayList<>();

        /*
            首先获users中所有以名字.obj的子项，然后遍历每个子项并用文件流连接对象输入流进行反序列化，最后将users对象存入到userList集合中
         */
        File[] subs = userDir.listFiles(f->f.getName().endsWith(".obj"));
        for(File userFile: subs){
            try(
                FileInputStream fis = new FileInputStream(userFile);
                ObjectInputStream ois = new ObjectInputStream(fis)
            ){
                userList.add((User)ois.readObject());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //2. 使用程序生成一个页面，同时遍历List集合将用户信息拼接到表格中
        try (PrintWriter pw = new PrintWriter("./userList.html");){
            pw.println("<!DOCTYPE html>");
            pw.println("<html lang=\"en\">");
            pw.println("<head>");
            pw.println("<meta charset=\"UTF-8\">");
            pw.println("<title>展示用户</title>");
            pw.println("</head>");
            pw.println("<body>");
            pw.println("<center>");
            pw.println("<h1>用户信息</h1>");
            pw.println("<table border=\"1\">");
            pw.println("<tr>");
            pw.println("<td>用户名</td>");
            pw.println("<td>密码</td>");
            pw.println("<td>昵称</td>");
            pw.println("<td>年龄</td>");
            pw.println("</tr>");

            for(User user: userList){
                pw.println("<tr>");
                pw.println("<td>"+ user.getUsername() +"</td>");
                pw.println("<td>" + user.getPassword() + "</td>");
                pw.println("<td>"+user.getNickname()+"</td>");
                pw.println("<td>"+user.getAge()+"</td>");
                pw.println("</tr>");
            }

            pw.println("</table>");
            pw.println("</center>");
            pw.println("</body>");
            pw.println("</html>");


            System.out.println("生成完毕");


        } catch (Exception e) {
            e.printStackTrace();
        }


        //3. 将生成的页面设置到响应中，发送给浏览器
        File file = new File("./userList.html");
        response.setContentFile(file);

    }
}
