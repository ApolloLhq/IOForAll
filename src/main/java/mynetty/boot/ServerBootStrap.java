package mynetty.boot;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

public class ServerBootStrap {
    private EventLoopGroup group;
    private EventLoopGroup chiledGroup;
    AcceptHandler sAcceptHander;
    public ServerBootStrap group(EventLoopGroup boss, EventLoopGroup worker) {
        group = boss;
        chiledGroup = worker;
        return this;
    }

    public void bind(int port) throws IOException {
        //bind 处理的是server的启动过程
        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind(new InetSocketAddress(port));
        sAcceptHander = new AcceptHandler(chiledGroup, server);
        EventLoop eventloop = group.choose();
        //把启动server，bind端口的操作变成task，推送到eventloop中执行。
        eventloop.execute(new Runnable() {
            @Override
            public void run() {
                eventloop.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            eventloop.name = Thread.currentThread() + eventloop.name;
                            System.out.println("bind...server...to " + eventloop.name);
                            server.register(eventloop.selector, SelectionKey.OP_ACCEPT, sAcceptHander);
                        } catch (ClosedChannelException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public static void main(String[] args) throws IOException {
        System.out.println(Thread.currentThread());

        EventLoopGroup boss = new EventLoopGroup(1);
        EventLoopGroup worker = new EventLoopGroup(3);
        ServerBootStrap b = new ServerBootStrap();
        b.group(boss, worker).bind(9090);

        System.in.read();
    }
}
