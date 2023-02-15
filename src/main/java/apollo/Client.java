package apollo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 8000);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintStream ps = new PrintStream(socket.getOutputStream());

        BufferedReader br2 = new BufferedReader(new InputStreamReader(socket .getInputStream()));//从服务器端接收到的消息

        while (true) {                                      //一直循环，一直到从客户端输入quit为止
            String string = br.readLine();
            ps.println(string);                           // 向服务器中发送消息
            ps.flush();
            if (string.trim().equals("quit"))// 退出
            {
                break;
            }

            // 从服务器端得到消息
            String serverString = br2.readLine();
            System.out.println("服务器：" + serverString);
        }
    }
}
