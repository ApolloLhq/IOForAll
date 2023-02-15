package apollo;

import java.io.*;

public class OSFileIO {
    static byte[] data = "123456789\n".getBytes();
    static String path =  "out.txt";
    public static void main(String[] args) throws InterruptedException, IOException {
        File file = new File(path);
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        while(true){
            Thread.sleep(10);
            out.write(data);
        }
    }
}
