package com.webserver.core;

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
            InputStream in = socket.getInputStream();
            int d;
            while((d = in.read()) != -1){
                System.out.print((char)d);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
