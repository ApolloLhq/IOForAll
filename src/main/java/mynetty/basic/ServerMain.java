package mynetty.basic;

public class ServerMain {
    public static void main(String[] args) {
        // 创建SelectorThread 一个或多个 （IO线程）
        SelectorThreadGroup group = new SelectorThreadGroup(1);
        // 混合模式，只有一个线程负责accept;每个线程都会被分配client,处理R\W
        //SelectorThreadGroup group = new SelectorThreadGroup();

        // 创建ServerSocketChannel并绑定到某个Selector上
        group.bind(8888);
    }
}
