package quick;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BIOServer {
    public static void main(String[] args) throws Exception {

        ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
        //创建socket服务,监听10101端口
        ServerSocket server = new ServerSocket(10101);
        System.out.println("服务器启动！");
        while(true){
            //获取一个套接字（阻塞）
            final Socket client = server.accept();
            System.out.println("来个一个新客户端！port:" + client.getPort());
			/*newCachedThreadPool.execute(new Runnable() {
				public void run() {
					//业务处理
					handler(client);
				}
			});*/
            //for debug
            handler(client);
        }
    }

    /**
     * 读取数据
     */
    public static void handler(Socket socket){
        try {
            InputStream inputStream = socket.getInputStream();
            byte[] bytes = new byte[5];

            while(true){
                //读取数据（阻塞）,当有足够数据填充字节数组时方法才会返回
                // telnet quit时才会返回-1跳出循环，否则在read方法上等待数据
                int read = inputStream.read(bytes);
                if(read != -1){
                    System.out.println(new String(bytes, 0, read));
                }else{
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                socket.close();
                System.out.println("socket关闭");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
