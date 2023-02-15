package mynetty.basic;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

// 管理一组IO线程
public class SelectorThreadGroup {
    
    SelectorThread[] selectorThreads;
    ServerSocketChannel server;
    AtomicInteger idx = new AtomicInteger(0);
    
    public SelectorThreadGroup(int threadNum) {
        selectorThreads = new SelectorThread[threadNum];
        for (int i = 0; i < threadNum; i++) {
            selectorThreads[i] = new SelectorThread(this);
            new Thread(selectorThreads[i]).start();  // 线程启动后就阻塞了
        }
    }

    public void bind(int port) {
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));

            // server注册到某个Selector
            bindSelector(server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void bindSelector(Channel channel) {
        SelectorThread selectorThread = nextSelector();
        // 往SelectorThread的队列中添加需要处理的channel
        selectorThread.queue.offer(channel);
        // wakeup 可以让selector.select()方法马上返回
        // 让SelectorThread线程的select方法返回去处理队列
        selectorThread.selector.wakeup();
        /*
         * selectorThread.selector.wakeup();
         * serverSocketChannel.register(selectorThread.selector, SelectionKey.OP_ACCEPT);   // 可能会被selector的select方法阻塞导致本线程不能继续执行
         * 但这种处理方式不合理，等待其他线程来处理的事(注册接收)应该交由对应线程来处理更合适，所以在SelectorThread引入队列
        */
    }

    private SelectorThread nextSelector() {
        int index = idx.getAndIncrement() % selectorThreads.length;
        return selectorThreads[index];
    }
}
