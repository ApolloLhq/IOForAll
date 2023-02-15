package apollo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class SelectorServer {

    private Selector selector;

    public void initServer(int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(port));

        this.selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void listen() throws IOException {
        System.out.println("服务端启动成功！");
        while (true) {
            selector.select();
            Iterator<?> ite = this.selector.selectedKeys().iterator();
            while (ite.hasNext()) {
                SelectionKey key = (SelectionKey) ite.next();
                ite.remove();

                handler(key);
            }
        }
    }

    /**
     * 处理请求
     */
    public void handler(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            handlerAccept(key);
        } else if (key.isReadable()) {
            handelerRead(key);
        }
    }

    /**
     * 处理连接请求
     */
    public void handlerAccept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel channel = server.accept();
        channel.configureBlocking(false);

        System.out.println("new client in!");
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    /**
     * 处理读的事件
     */
    public void handelerRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = channel.read(buffer);
        if(read > 0){
            byte[] data = buffer.array();
            String msg = new String(data).trim();
            System.out.println("server recv：" + msg);

            ByteBuffer outBuffer = ByteBuffer.wrap("ok!".getBytes());
            channel.write(outBuffer);// 将消息回送给客户端
        }else{
            System.out.println("client close!");
            channel.close();
            key.cancel();
        }
    }

    /**
     * 启动服务端测试
     */
    public static void main(String[] args) throws IOException {
        SelectorServer server = new SelectorServer();
        server.initServer(8000);
        server.listen();
    }
}
