package apollo;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MMap {
    static String path =  "out.txt";

    public static void main(String[] args) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(path, "rw");
        raf.write("0123456789\n".getBytes());   // 系统调用write，未真正写入磁盘
        raf.write("abcdefghijk\n".getBytes());

        System.out.println("write------------");
        System.in.read();

        raf.seek(4); // 随机位置读写
        raf.write("ooxx".getBytes());

        System.out.println("seek---------");
        System.in.read();

        // lsof -op pid	pid是对应的java进程id(jps),可以查看文件描述符，也能看到mmap对应的fd
        FileChannel rafchannel = raf.getChannel();
        // mmap 产生堆外且文件映射到内核PageCache空间的 ByteBuffer对象
        MappedByteBuffer map = rafchannel.map(FileChannel.MapMode.READ_WRITE, 0, 4096);
        map.put("@@@".getBytes());  // 不会产生系统调用，但数据会到达内核PageCache
        // 非mmap想要将data写入内核PageCache，调用out.write()，会产生系统调用，必须有用户态内核态切换
        // mmap映射的PageCache，也可能会丢数据
        // Direct IO才能跳过Linux的PageCache，但需要自己管理内存数据，维护一致性、脏数据等问题
        // map.force(); //  flush 将PageCache中的数据强制写入物理磁盘

        System.out.println("map--put--------");

        // OS没有绝对的数据可靠性，PageCache是为了减少硬件IO调用，优先使用内存的文件
        // 即使选择可靠性，调成最慢的方式（每write一次flush一次），但是单点问题会让性能损耗严重，不值得
        // 所以现在的分布式系统会做 主从复制、主备HA
    }
}
