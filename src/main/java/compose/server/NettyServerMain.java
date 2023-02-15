package compose.server;

import compose.MyReadHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class NettyServerMain {
    public static void main(String[] args) throws InterruptedException {
        // 事件循环组，是一个线程池，每个事件循环线程绑定一个Selector
        // 也能当普通线程来用
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        NioServerSocketChannel server = new NioServerSocketChannel();

        group.register(server);
        ChannelPipeline pipeline = server.pipeline();
        // pipeline.addLast(new MyAcceptHandler(group, new MyReadHandler())); // 这里所有通道公用了MyReadHandler对象，会抛出异常
        pipeline.addLast(new MyAcceptHandler(group, new MyChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new MyReadHandler());
            }
        }));

        ChannelFuture future = server.bind(new InetSocketAddress(8080));
        // future.sync().channel().closeFuture().sync();
        server.closeFuture().sync();
    }
}
