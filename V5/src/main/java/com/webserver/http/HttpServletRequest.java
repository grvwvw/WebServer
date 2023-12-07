package com.webserver.http;

import org.omg.CORBA.PRIVATE_MEMBER;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求对象
 * 该类的每一个实例用于表示一个HTTP请求
 * 每个请求由三部分主成：请求行，消息头，消息正文
 */
public class HttpServletRequest {
    private Socket socket;

    //请求行相关信息
    private String method; //请求方式
    private String uri; //抽象路径
    private String protocol; //协议版本
    private Map<String, String> headers = new HashMap<>(); //消息头相关信息

    public HttpServletRequest(Socket socket) throws IOException {
        this.socket = socket;

        //1.1 解析请求行
        parseRequestLine();

        //1.2 解析消息头
        parseHeaders();

        //1.3 解析消息正文
        parseContent();
    }

    /**
     * 解析请求行
     * parse: 解析
     */
    private void parseRequestLine() throws IOException {
        String line = readLine();
        System.out.println(line);

        String[] parts = line.split("\\s");
        method = parts[0];
        uri = parts[1];
        protocol = parts[2];

        System.out.println("method: " + method);
        System.out.println("uri: " + uri); //可能会出现下标越界异常，浏览器请求空
        System.out.println("protocol: " + protocol);
    }

    /**
     * 解析消息头
     */
    private void parseHeaders() throws IOException {
        String line = readLine();
        while(true){ //.优先级比=要高
            line = readLine();
            if(line.isEmpty()) break;

            String[] s = line.split(":\\s"); //根据:进行拆分
            headers.put(s[0], s[1]);
        }

        System.out.println("headers:" + headers);
        // headers.forEach((k, v)-> System.out.println(k + ":" + v));
    }

    /**
     * 解析消息正文
     */
    private void parseContent(){}


    /**
     * 通过socket获取的输入流读取客户端发送过来的一行字符串
     * @return
     */
    private String readLine() throws IOException { //读取一行操作
        InputStream in = socket.getInputStream();
        char pre = 'a', cur = 'a';
        StringBuilder builder = new StringBuilder();
        int d;
        while((d = in.read()) != -1) //循环读入当前的字符
        {
            cur = (char)d;
            if(pre == 13 && cur == 10) // 如果上一个字符不是回车（13 CR）且当前字符不是换行（10 LF），则一直执行循环
                break;
            builder.append(cur);
            pre = cur;
        }
        return builder.toString().trim(); //使用trim()是为了删除字符串最后的一个CR
    }
}
