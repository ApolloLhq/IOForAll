package mynetty.boot;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class EventLoop implements Executor {

    String name;

    Thread thread;

    Selector selector;

    BlockingQueue<Runnable> events;

    public EventLoop(String name) {
        this.name = name;
        this.events = new LinkedBlockingQueue<>();
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(Runnable task) {
        try {
            events.put(task);
            this.selector.wakeup();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!inEventLoop()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        thread = Thread.currentThread();
                        EventLoop.this.run();
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    //Loop
    public void run() throws InterruptedException, IOException {
        System.out.println("loop runing");
        for ( ; ; ) {
            //select
            int nums = selector.select(); //会一直阻塞，不过可以通过外界有task到达来wakeup唤醒
            //selectedkeys to events
            if (nums > 0) {
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    Handler handler = (Handler) key.attachment();
                    /*if (handler instanceof AcceptHandler) {

                    } else if (handler instanceof ReadHandler) {

                    }*/
                    handler.handle();
                }
            }
            //run events
            runTask();
        }
    }

    public void runTask() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            Runnable task = events.poll(10, TimeUnit.MILLISECONDS);
            if (task != null) {
                events.remove(task);
                task.run();
            }
        }
    }

    private boolean inEventLoop() {
        return thread == Thread.currentThread();
    }
}
