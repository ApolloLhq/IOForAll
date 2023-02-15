package apollo;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BIOServer {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(8080);
        System.out.println("server up use 8080!");

        System.in.read();

        Socket client = server.accept();
        System.out.println("client port: " + client.getPort());

        new Thread(() -> {
            try {
                byte[] bytes = new byte[1024];
                InputStream inputStream = client.getInputStream();
                while(true){
                    int read = inputStream.read(bytes);
                    if(read != -1){
                        System.out.println(new String(bytes, 0, read));
                    }else{
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
