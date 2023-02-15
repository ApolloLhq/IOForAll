package mynetty.basic;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

public class SelectorThread implements Runnable{
    // 每个线程对应一个Selector，客户端会被分配到多个Selector上
    // 每个客户端只会绑定到一个Selector上，每个Selector互不干扰

    Selector selector = null;
    LinkedBlockingQueue<Channel> queue = new LinkedBlockingQueue<>();
    SelectorThreadGroup belongGroup;

    public SelectorThread(SelectorThreadGroup group) {
        try {
            this.selector = Selector.open();
            this.belongGroup = group;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // loop
        while (true) {
            try {
                int num = selector.select();    // 线程因调用系统调用而被挂起（还是RUNNABLE状态），持有锁；其他线程使用该selector的一些方法没有获得锁会进入BLOCKED状态而阻塞住
                if (num > 0) {
                    // 处理selectKeys
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    if (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();

                        if (key.isAcceptable()) {
                            handleAccept(key);
                        } else if (key.isReadable()) {
                            handleRead(key);
                        } else if (key.isWritable()) {
                            handleWrite(key);
                        }
                    }
                }
                // 处理队列
                while (queue.size() > 0) {
                    Channel channel = queue.take();
                    if (channel instanceof ServerSocketChannel) {
                        ServerSocketChannel server = (ServerSocketChannel) channel;
                        server.register(selector, SelectionKey.OP_ACCEPT);
                    } else if (channel instanceof SocketChannel) {
                        SocketChannel client = (SocketChannel) channel;
                        ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
                        client.register(selector, SelectionKey.OP_READ, buffer);
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleWrite(SelectionKey key) {

    }

    private void handleRead(SelectionKey key) {
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        SocketChannel client = (SocketChannel) key.channel();
        buffer.clear();
        while (true) {
            try {
                int len = client.read(buffer);
                if (len > 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        client.write(buffer);
                    }
                    buffer.clear();
                } else if (len == 0) {
                    break;
                } else {
                    // 客户端断开
                    System.out.println("client closed!!");
                    client.close();
                    key.cancel();
                    break;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleAccept(SelectionKey key) {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        try {
            SocketChannel client = server.accept();
            client.configureBlocking(false);

            // choose a selector and registered
            belongGroup.bindSelector(client);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
