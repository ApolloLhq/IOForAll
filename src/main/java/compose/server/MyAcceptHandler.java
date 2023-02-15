package compose.server;

import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * netty中已有实现，不需要使用者注册NioServerSocketChannel的handler
 */
public class MyAcceptHandler extends ChannelInboundHandlerAdapter {
    private EventLoopGroup group;
    private ChannelHandler handler;

    public MyAcceptHandler(EventLoopGroup group, ChannelHandler handler) {
        this.group = group;
        this.handler = handler;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("server registerd...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // NioServerSocketChannel接收到的应该是客户端的Channel
        NioSocketChannel client = (NioSocketChannel) msg;
        // 客户端的Handler
        ChannelPipeline pipeline = client.pipeline();
        // 每个客户端都注册了同一个handler,如果多个客户端访问会报错；可以给handler加注解@ChannelHandler.Sharable
        // handler如果是单例的，使用者想要在客户端绑定属性是不被允许的，所以最好是多例的
        // 可以设计一个无关业务的handler，让使用者自己new一个handler（参考MyChannelInitializer）
        pipeline.addLast(handler);  // pipeline:[channelInitialzer(共享),MyReadHandler]

        // 注册
        this.group.register(client);
    }
}
