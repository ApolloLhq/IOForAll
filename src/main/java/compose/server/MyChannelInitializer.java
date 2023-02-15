package compose.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public abstract class MyChannelInitializer<C extends Channel> extends ChannelInboundHandlerAdapter {
    // 策略模式，使用者自己定义pipeline的handler
    protected abstract void initChannel(C ch) throws Exception;

    @Override
    @SuppressWarnings("unchecked")
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        initChannel((C) ctx.channel());
        ctx.pipeline().remove(this);    // 初始化channel的pipeline后从channel中移除本对象
    }
}
