浏览器在连接的时候可能会不发请求，此时就需要服务器处理空请求，因此在处理请求行的时候会接收不到，出现下标越界

因此在第一行读入为空的时候，设置一个异常抛出到方法之外，然后在外部调用时，使用catch捕捉该异常，并且退出程序断开连接