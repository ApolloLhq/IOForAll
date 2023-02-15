package mynetty.boot;

import java.util.concurrent.atomic.AtomicInteger;

public class EventLoopGroup {
    AtomicInteger cid = new AtomicInteger(0);
    EventLoop[] children = null;

    EventLoopGroup(int nThreads) {
        children = new EventLoop[nThreads];
        for (int i = 0; i < nThreads; i++) {
            children[i] = new EventLoop("EventLoop_" + i);
        }
    }

    public EventLoop choose() {
        return children[cid.getAndIncrement() % children.length];
    }
}
