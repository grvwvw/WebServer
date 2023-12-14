package com.webserver.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 响应对象
 * 该类的每一个实例用于表示HTTP协议要求的响应，每个响应由三部分组成：
 * 状态行，响应头，响应正文
 *
 */
public class HttpServletResponse {
    private Socket socket;
    // 状态行相关信息
    private int statusCode = 200;
    private String statusReason = "OK";

    //响应头相关信息
    private Map<String, String> headers = new HashMap<>(); // 创建响应头响应的键值对

    // 响应正文相关信息
    private File contentFile;

    public HttpServletResponse(Socket socket){
        this.socket = socket;
    }

    /**
     * 将当前响应对象内容按照标准的HTTP响应格式发送给客户端
     */
    public void response() throws IOException {
        //3.1 发送请求行
        sendStatusLine();

        //3.2 发送响应头
        sendHeaders();

        //3.3 发送响应正文(index.html页面中的数据)
        sendContent();

    }

    /**
     * 发送请求行
     */
    private void sendStatusLine() throws IOException {
        println("HTTP/1.1 " + statusCode + " " + statusReason);
    }

    /**
     * 发送响应头
     */
    private void sendHeaders() throws IOException {
        println("Content-Type: text/html");
        println("Content-Length: " + contentFile.length());
        println(""); // 单独再发送一个CRLF，因此在此输入一个空字符串到方法
    }

    /**
     * 发送响应正文
     */
    private void sendContent() throws IOException {
        OutputStream out = socket.getOutputStream();
        FileInputStream fis = new FileInputStream(contentFile);
        int len;
        byte[] data = new byte[1024 * 10];
        while((len = fis.read(data)) != -1){
            out.write(data, 0, len); //使用块读块写的方法
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

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public File getContentFile() {
        return contentFile;
    }

    public void setContentFile(File contentFile) {
        this.contentFile = contentFile;
    }

    /**
     * 添加一个要发送的响应头
     * @param name
     * @param value
     */
    public void addHeader(String name, String value){
        // 将响应头的名字和值以key-value的形式存入到headers这个Map中
        headers.put(name, value);
    }
}
