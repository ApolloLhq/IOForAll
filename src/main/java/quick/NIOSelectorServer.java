package quick;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * NIO服务端，使用了Selector
 * 这里使用的单线程的模式，多线程环境下会重复处理SelectKey，进而会产生一些问题，然而使用key.cancel不仅增加系统调用，同时它将整个channel移除了，只处理了一次
 */
public class NIOSelectorServer {
	// 通道管理器
	private Selector selector;

	private static ExecutorService executorService = Executors.newCachedThreadPool();

	/**
	 * 获得一个ServerSocket通道，并对该通道做一些初始化的工作
	 */
	public void initServer(int port) throws IOException {
		// 获得一个ServerSocket通道
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		// 设置通道为非阻塞
		serverChannel.configureBlocking(false);
		// 将该通道对应的ServerSocketChannel绑定到port端口
		serverChannel.socket().bind(new InetSocketAddress(port));
		// 获得一个通道管理器
		this.selector = Selector.open();
		// 将channel和selector绑定，并为该通道注册SelectionKey.OP_ACCEPT事件
		// 当通道触发注册在selector上的事件，selector.select()会返回，如果没有就绪通道,selector.select()会一直阻塞。
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
	}

	/**
	 * 采用轮询的方式监听selector上是否有就绪的Channel，如果有，则进行处理
	 */
	public void listen() throws IOException {
		System.out.println("服务端启动成功！");
		// 轮询访问selector
		while (true) {
			// 当注册的事件到发生，方法返回；否则,该方法会一直阻塞
			selector.select();
			// 获得selector中筛选结果集的迭代器，每个SelectionKey绑定一个Channel
			Iterator<?> ite = this.selector.selectedKeys().iterator();
			while (ite.hasNext()) {
				SelectionKey key = (SelectionKey) ite.next();
				// 删除已选的key,以防重复处理,结果集会重复添加
				ite.remove();

				/*executorService.execute(new Runnable() {
					// 这样直接使用线程池多线程处理会有问题，异步后再次调用select方法时也会取到selectKey并处理,多次接收（null）,多次读写
					@Override
					public void run() {
						try {
							handler(key);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});*/
				handler(key);
			}
		}
	}

	/**
	 * 处理请求
	 */
	public void handler(SelectionKey key) {
		// 客户端请求连接事件
		if (key.isAcceptable()) {
			handlerAccept(key);
			// 获得了可读的事件
		} else if (key.isReadable()) {
			// 如果不在select的线程中cancel，多线程环境下会重复触发，并且移除后就不再监听读取事件了
			// 但cancel实际上调用系统调用epoll_ctl，频繁的cancel会损耗性能
			/*key.cancel();
			executorService.submit(() -> handleRead(key));*/
			handleRead(key);
		} else if (key.isWritable()) {
			/*key.cancel();
			executorService.submit(() -> handleWrite(key));*/
			// 注册关注SelectionKey.OP_WRITE后，实际是检查send queue是否为空，只要是空的会一直会有写事件
			// 什么时候写是依赖于逻辑的，而不是依赖send queue是否为空，当需要写才注册写事件，不需要时调用key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE)，如果不取消会一直调用
			handleWrite(key);
		}
	}

	private void handleWrite(SelectionKey key) {
        System.out.println("handle write!");
		// 服务器可写消息:得到事件发生的Socket通道
		SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer buffer = (ByteBuffer) key.attachment();
		if (buffer == null) return;
		while (buffer.hasRemaining()) {
			try {
				client.write(buffer);
			} catch (IOException e) {
				e.printStackTrace();
				key.cancel();
			}
		}
		buffer.clear();
		key.attach(null);
		key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);	// 取消key关注的写事件

	}

	/**
	 * 处理连接请求
	 */
	private void handlerAccept(SelectionKey key) {
		ServerSocketChannel server = (ServerSocketChannel) key.channel();
		try {
			// 获得和客户端连接的通道
			SocketChannel channel = server.accept();
			// 设置成非阻塞
			channel.configureBlocking(false);

			// 在这里可以给客户端发送信息哦
			System.out.println("新的客户端连接");
			// 在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限。
			channel.register(this.selector, SelectionKey.OP_READ);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理读的事件
	 */
	public void handleRead(SelectionKey key) {
		// 服务器可读取消息:得到事件发生的Socket通道
		SocketChannel channel = (SocketChannel) key.channel();
		// 创建读取的缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		
		// 此处存在问题，如果客户端关闭，则这里抛出异常，而若注释回写数据的代码，关掉客户端后程序将死循环读空数据
		// debug发现关闭客户端后read方法返回的是-1,此时我们应该控制程序退出方法了
		// 注释回写代码后死循环应该是程序没有发现客户端关闭，所以一直在读
		/*
		int len = channel.read(buffer);
		byte[] data = buffer.array();
		String msg = new String(data).trim();
		System.out.println("服务端收到信息：" + msg);
		
		//回写数据
		ByteBuffer outBuffer = ByteBuffer.wrap("好的".getBytes());
		channel.write(outBuffer);// 将消息回送给客户端
		 */		 
		
		// 此处只读了一次，所以数据过长将在下次的轮询中读取。
		// 而若想在这里使用循环将数据读完，若还有数据因某些原因还没发送过来(数据没准备好)，read方法也会马上返回0，等数据发送过来会触发通道的读事件，这样就不会阻塞线程了。(参考NServer)
		// 客户端正常关闭返回-1，非正常关闭抛出异常
		// 此处有个细节：读事件触发后，read方法读的数据包括触发读事件后的数据。
		// 正确处理数据应该是使用try-catch语句包住read方法，抛出异常为客户端异常退出，使用循环的方式读取数据，read方法返回-1同样是客户端关闭了
		try {
			int read = channel.read(buffer);	// telnet quit指令抛出异常
			if(read > 0){
				byte[] data = buffer.array();
				String msg = new String(data).trim();
				System.out.println("服务端收到信息：" + msg);

				// 将消息回送给客户端
				ByteBuffer outBuffer = ByteBuffer.wrap("好的".getBytes());
				// 回写方式1
				// channel.write(outBuffer);
				// 回写方式2
				channel.register(key.selector(), key.interestOps() | SelectionKey.OP_WRITE, outBuffer);
			}else{
				System.out.println("客户端关闭");
				channel.close();
				key.cancel();
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				channel.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
			key.cancel();
		}
	}

	/**
	 * 启动服务端测试
	 */
	public static void main(String[] args) throws IOException {
		NIOSelectorServer server = new NIOSelectorServer();
		server.initServer(8000);
		server.listen();
	}

}
