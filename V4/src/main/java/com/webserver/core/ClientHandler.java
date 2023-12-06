package com.webserver.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

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
            //1.1 解析请求行
            String line = readLine();
            System.out.println(line);

            String[] parts = line.split("\\s");
            String method = parts[0];
            String uri = parts[1];
            String protocol = parts[2];

            System.out.println("method: " + method);
            System.out.println("uri: " + uri); //可能会出现下标越界异常，浏览器请求空
            System.out.println("protocol: " + protocol);

            //1.2 解析消息头
            while(!(line = readLine()).isEmpty()){ //.优先级比=要高
                System.out.println(line);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
