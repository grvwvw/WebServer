package com.webserver.controller;

import com.webserver.core.DispatcherServlet;
import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.*;
import java.net.URISyntaxException;

/**
 * 处理与文章相关的业务
 */
public class ArticleController {
    private static File articleDir;
    private static File root;
    private static File staticDir;

    static {
        try {
            root = new File(DispatcherServlet.class.getClassLoader().getResource(".").toURI()); //定位到了resources目录
            staticDir = new File(root, "static");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        articleDir = new File("./articles");
        if(!articleDir.exists()){
            articleDir.mkdirs();
        }
    }
    public void writeArticle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //1. 获取表单数据
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String content = request.getParameter("content");

        if(title == null || author == null || content == null){
            File file = new File(staticDir, "/myweb/article_fail.html");
            response.setContentFile(file);
            return;
        }

        //2. 保存文章
        File articleFile = new File(articleDir, title + ".obj");
        if(!articleFile.exists()){
            FileOutputStream fos = new FileOutputStream(articleFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

        }
    }
}
