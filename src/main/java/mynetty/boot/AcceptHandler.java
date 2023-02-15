package mynetty.boot;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class AcceptHandler implements Handler{
    ServerSocketChannel key;
    EventLoopGroup cGroup;
    AcceptHandler(EventLoopGroup cGroup, ServerSocketChannel server) {
        this.key = server;
        this.cGroup = cGroup;
    }
    public void handle() {
        try {
            final EventLoop eventLoop = cGroup.choose();
            final SocketChannel client = key.accept();
            client.configureBlocking(false);
            client.setOption(StandardSocketOptions.TCP_NODELAY,true);
            final ReadHandler cHandler = new ReadHandler(client);
            eventLoop.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        eventLoop.name = Thread.currentThread() + eventLoop.name;
                        System.out.println("socket...send...to " + eventLoop.name);

                        client.register(eventLoop.selector, SelectionKey.OP_READ, cHandler);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
