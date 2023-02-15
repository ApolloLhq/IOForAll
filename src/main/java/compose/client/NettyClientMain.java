package compose.client;

import compose.MyReadHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException {
        // 事件循环组，是一个线程池，每个线程绑定一个Selector
        // 也能当普通线程来用
        NioEventLoopGroup group = new NioEventLoopGroup(1);

        NioSocketChannel client = new NioSocketChannel();
        // 绑定事件循环组
        group.register(client);

        ChannelPipeline pipeline = client.pipeline();
        pipeline.addLast(new MyReadHandler());

        ChannelFuture future = client.connect(new InetSocketAddress("127.0.0.1", 8080));
        future.sync();

        ByteBuf buf = Unpooled.copiedBuffer("hello server".getBytes());
        // netty的写必须依赖NioEventLoop，即需要绑定Selector
        ChannelFuture writeFuture = client.writeAndFlush(buf);
        writeFuture.sync();

        // future.channel().closeFuture().sync();
        client.closeFuture().sync();    // 等待服务端将客户端关闭
    }
}
