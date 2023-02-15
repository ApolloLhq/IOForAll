package quick;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

public class NIOServer {
    private ServerSocketChannel serverSocketChannel;
    private List<SocketChannel> socketChannels;

    public void initServer(int port) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        socketChannels = Lists.newArrayList();
        listen();
    }

    private void listen() throws IOException{
        System.out.println("服务端启动成功！");
        while (true) {
            // 处理事件
            Iterator<SocketChannel> iterator = socketChannels.iterator();
            while (iterator.hasNext()) {
                if (handleRead(iterator.next())) continue;
                iterator.remove();
            }
            handleAccept();
        }
    }

    private void handleAccept() throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        if (socketChannel != null) {
            socketChannel.configureBlocking(false);
            socketChannels.add(socketChannel);
            System.out.println("新客户端连接!");
        }
    }

    private boolean handleRead(SocketChannel socketChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int len;
        while((len = socketChannel.read(buffer)) > 0 ){
            System.out.println("服务端收到信息：" + new String(buffer.array(), 0, len, StandardCharsets.UTF_8));
            handleWrite("收到", socketChannel);
        }
        if (len < 0) {
            System.out.println("客户端退出！");
            return false;
        }
        return true;
    }

    private void handleWrite(String msg, SocketChannel socketChannel) throws IOException{
        socketChannel.write(ByteBuffer.wrap(msg.getBytes()));
    }

    public static void main(String[] args) throws IOException {
        new NIOServer().initServer(8080);
    }
}
