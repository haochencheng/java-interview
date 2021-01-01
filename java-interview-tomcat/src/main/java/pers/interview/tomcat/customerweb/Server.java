package pers.interview.tomcat.customerweb;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 自定义web服务器
 *
 * @description:
 * @author: haochencheng
 * @create: 2019-07-24 15:11
 **/
public class Server {

    private static final int PORT = 10234;
    // 关闭服务命令
    private static final String SHUTDOWN_COMMAND = "/shutDown";

    private Boolean start=true;

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.await();
    }

    private void await() {
        ServerSocket serverSocket = null;
        try {
//            serverSocket = new ServerSocket(PORT, 1, InetAddress.getByName("127.0.0.1"));
            serverSocket = new ServerSocket(PORT, 1, InetAddress.getByName("localhost"));
//            serverSocket = new ServerSocket(PORT, 1, InetAddress.getByName("0.0.0.0"));
//            serverSocket = new ServerSocket(PORT, 1, InetAddress.getByName("10.244.15.227"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Socket socket;
        InputStream inputStream ;
        OutputStream outputStream ;
        while (start) {
            try {
                socket = serverSocket.accept();
                //输入
                inputStream = socket.getInputStream();
                parse(inputStream);
                //输出
                outputStream = socket.getOutputStream();
                // 用 writer 对客户端 socket 输出一段 HTML 代码
                PrintWriter out = new PrintWriter(outputStream);
                String response="HTTP/1.0 200 OK\r\nContent-Type:text/html\r\n\r\nhello,world!\r\n";
                System.out.println(response);
                out.println(response);
                out.close();
                // 关闭 socket 对象
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 从InputStream中读取request信息，并从request中获取uri值
     * @param inputStream
     */
    public void parse(InputStream inputStream) {
        StringBuffer request = new StringBuffer(2048);
        int i;
        byte[] buffer = new byte[2048];
        try {
            i = inputStream.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            i = -1;
        }
        for (int j = 0; j < i; j++) {
            request.append((char) buffer[j]);
        }
        System.out.print(request.toString());
    }




}
