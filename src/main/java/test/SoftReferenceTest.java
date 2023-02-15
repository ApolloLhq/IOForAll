package test;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public class SoftReferenceTest {
    public static void main(String[] args) throws InterruptedException {
        List<SoftReference<MyObj>> list = new ArrayList<>();
        MyObj myObj = new MyObj();
        final ReferenceQueue<Object> queue = new ReferenceQueue<>();
        SoftReference<MyObj> reference = new SoftReference<>(myObj, queue);

        new Thread(() -> {
            try {
                System.err.println(queue.remove());
                System.exit(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        myObj = null;

        System.gc();    // full GC未被回收

        //TimeUnit.SECONDS.sleep(10);

        while (true) {
            list.add(new SoftReference<>(new MyObj()));
        }

        //System.out.println(reference.get());
    }

    private static class MyObj{
        private byte[] arr = new byte[1024]; // 1k

        public MyObj() {}

        public byte[] getArr() {
            return arr;
        }

        public void setArr(byte[] arr) {
            this.arr = arr;
        }

        /*@Override
        protected void finalize() throws Throwable {
            System.out.println("finalize called!");
        }*/
    }
}
