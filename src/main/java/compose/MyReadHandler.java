package compose;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class MyReadHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        // CharSequence str = buf.readCharSequence(buf.readableBytes(), CharsetUtil.UTF_8);    // 会移动指针
        CharSequence str = buf.getCharSequence(0, buf.readableBytes(), CharsetUtil.UTF_8);
        System.out.println(str);
        // 响应
        ctx.writeAndFlush(buf);
    }
}
