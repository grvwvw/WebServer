package com.webserver.http;

import org.omg.CORBA.PRIVATE_MEMBER;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
    private String requestURI; // 保存uri？左侧的内容
    private String queryString; // 保存uri ？右侧的部分，也就是参数部分
    private Map<String, String> parameters = new HashMap<>(); //保存客户端传递过来的每一组参数
    private Map<String, String> headers = new HashMap<>(); //消息头相关信息

    public HttpServletRequest(Socket socket) throws IOException, EmptyRequestException {
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
    private void parseRequestLine() throws IOException, EmptyRequestException {
        String line = readLine();
        if (line.isEmpty()) { //判断是否为空请求，也就是业务逻辑中直接断开连接就可以
            throw new EmptyRequestException();
        }

        System.out.println(line);

        String[] parts = line.split("\\s");
        method = parts[0];
        uri = parts[1];
        protocol = parts[2];

        // 进一步解析uri
        parseUri();

        System.out.println("method: " + method);
        System.out.println("uri: " + uri); //可能会出现下标越界异常，浏览器请求空
        System.out.println("protocol: " + protocol);
    }

    /**
     * 进一步解析URI
     */
    private void parseUri(){
        /**
         * uri有参数是两种情况：1.不含参数的  2.含参数的
         * 例如：
         * 1. 不含参数的/myweb/reg.html
         * 2. 含参数的/myweb/reg?username=fndaiuwo&password=123455&nickname=daddy&age=22
         */
        String[] data = uri.split("\\?");
        requestURI = data[0];
        if(data.length > 1){
            queryString = data[1];

            // 调用parseParameter(String line)方法，将参数的格式为: name1=value1&name2=value2&name3=...的line解析
            parseParameter(queryString);
        }

        System.out.println("requestURI:" + requestURI);
        System.out.println("queryString:" + queryString);
        System.out.println("parameters:" + parameters);
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

//            headers.put(s[0].toLowerCase(), s[1]); 通过将所有的key修改为小写，那么后续获取的时候就可以不区分大小写，全部通过全小写访问内容
        }

        // System.out.println("headers:" + headers);
        headers.forEach((k, v)-> System.out.println("消息头: " + k + ":" + v));
    }

    /**
     * 解析消息正文
     */
    private void parseContent() throws IOException {
        /**
         * 当一个请求的请求方式为POST时，则说明会包含消息正文
         */
        if("POST".equalsIgnoreCase(method)){
            // 判断消息头中是否有Content-Length
            if(headers.containsKey("Content-Length")){
                int contentLength = Integer.parseInt(headers.get("Content-Length"));
                System.out.println("===========>" + contentLength);

                // 根据Content-Length创建对应长度的字节数组
                byte[] data = new byte[contentLength];
                InputStream in = socket.getInputStream();
                in.read(data); //将正文内容读进data数组

                // 判断消息头Content-Type判断正文的类型，并对此进行解析
                if(headers.containsKey("Content-Type")){
                    String contentType = headers.get("Content-Type");
                    System.out.println("============>" + contentType);

                    if("application/x-www-form-urlencoded".equals(contentType)){ // 该类型是用于获取表单的POST请求提交的
                        String line = new String(data, StandardCharsets.ISO_8859_1);
                        System.out.println("正文内容=========>" + line);

                        // 调用parseParameter(String line)方法，将参数的格式为: name1=value1&name2=value2&name3=...的line解析
                        parseParameter(line);

                    } // else if(){} 后期可以扩展其他类型的POST请求
                }
            }
        }
    }

    /**
     * 解析参数，参数的格式为: name1=value1&name2=value2&name3=...
     * 将每一组参数的参数名作为key，参数值作为value存入parameters中
     *
     * line的格式可能为: username=%E8%8C%E4&A5&password=123456   //这里边使用十六进制解析中文内容
     * @param line
     */
    private void parseParameter(String line){
        try {
            line = URLDecoder.decode(line, "UTF-8"); //这样解码之后，注册的username就可以是中文啦
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        String[] data = line.split("&");
        for(String para: data){
            String[] paras = para.split("=");
            parameters.put(paras[0], paras.length > 1? paras[1]: null);
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

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHeader(String name){ // 获取的是消息头中key对应的value
        return headers.get(name);
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getQueryString() {
        return queryString;
    }

    /**
     * 根据参数名获取对应的参数值
     * @param name
     * @return
     */
    public String getParameter(String name){
        return parameters.get(name);
    }
}
