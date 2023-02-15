package compose;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class ByteBufTest {
    public static void main(String[] args) {
        // ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10, 20);
        // ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.heapBuffer(10, 20);
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(10, 20);
        print(buf);

        buf.writeBytes(new byte[]{1, 2, 3, 4, 5});
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4, 5});
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4, 5});
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4, 5});
        print(buf);
        // buf.writeBytes(new byte[]{1, 2, 3, 4, 5});
    }

    public static void print(ByteBuf buf) {
        System.out.println("isReadable: " + buf.isReadable());
        System.out.println("readerIndex: " + buf.readerIndex());
        System.out.println("readableBytes: " + buf.readableBytes());
        System.out.println("isWritable: " + buf.isWritable());
        System.out.println("writerIndex: " + buf.writerIndex());
        System.out.println("writableBytes: " + buf.writableBytes());
        System.out.println("capacity: " + buf.capacity());
        System.out.println("maxCapacity: " + buf.maxCapacity());
        System.out.println("isDirect: " + buf.isDirect());
        System.out.println("-------------------------------------------------------------------");
    }
}
