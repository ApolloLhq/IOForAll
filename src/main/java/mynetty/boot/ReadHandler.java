package mynetty.boot;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ReadHandler implements Handler{
    SocketChannel key;
    ReadHandler(SocketChannel server) {
        this.key = server;
    }

    @Override
    public void handle() {
        ByteBuffer data = ByteBuffer.allocateDirect(4096);
        try {
            key.read(data);
            data.flip();
            byte[] dd = new byte[data.limit()];
            data.get(dd);
            System.out.println(new String(dd));
            data.clear();
            for (int i = 0; i < 10; i++) {
                data.put("a".getBytes());
                data.flip();
                key.write(data);
                data.clear();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
