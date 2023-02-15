package test;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.TimeUnit;

public class PhantomReferenceTest {
    public static void main(String[] args) throws InterruptedException {
        MyObj myObj = new MyObj();
        final ReferenceQueue<Object> queue = new ReferenceQueue<>();
        PhantomReference<MyObj> reference = new PhantomReference<>(myObj, queue);

        new Thread(() -> {
            try {
                System.err.println(queue.remove());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        myObj = null;

        int i = 1;
        while (true) {
            System.out.println("第" + i++ + "次GC");
            System.gc();
            TimeUnit.SECONDS.sleep(5);
        }
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
