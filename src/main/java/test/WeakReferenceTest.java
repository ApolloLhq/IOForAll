package test;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

public class WeakReferenceTest {
    // WeakReference在young GC中就可能被回收
    public static void main(String[] args) throws InterruptedException {
        MyObj myObj = new MyObj();
        final ReferenceQueue<Object> queue = new ReferenceQueue<>();
        WeakReference<MyObj> reference = new WeakReference<>(myObj, queue);

        new Thread(() -> {
            try {
                System.err.println(queue.remove());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        myObj = null;

        /*for (int i = 0; i < 3400; i++) {
            int[] arr = new int[1024];
        }*/

        int i = 1;
        while (true) {
            System.out.println("第" + i++ + "次GC");
            System.gc();
            TimeUnit.SECONDS.sleep(5);
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

        @Override
        protected void finalize() throws Throwable {
            System.out.println("finalize called!");
        }
    }
}
