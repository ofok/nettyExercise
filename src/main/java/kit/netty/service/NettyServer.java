package kit.netty.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {
    public static void main(String[] args) {
        //  创建两个线程组bossGroup 和 workerGroup，含有的子线程NioEventLoop 的个数默认为cpu核数的两倍
        // bossGroup 只是处理链接请求，真正的和客户端业务处理，会交给workerGroup完成
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 创建服务器端的启动对象
            ServerBootstrap bootstrap = new ServerBootstrap();
            // 使用链式编程来配置参数
            bootstrap.group(bossGroup, workerGroup) // 设置两个线程组
                    .channel(NioServerSocketChannel.class)  // 使用NioServerSocketChannel作为服务器的通道实现
                    // 初始化服务器连接队列大小，服务器处理客户端连接请求是顺序处理的，所以同一时间只能处理一个客户端连接
                    // 多个客户端同时来的时候，服务端将不能处理的客户端连接请求放在队列中等待处理
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 对workerGroup 的socketChannel 设置处理器codec
                            socketChannel.pipeline().addLast(new NettyServerHandler());
                        }
                    });
            System.out.println("netty server start...");
            // 绑定一个端口并且同步，生成了一个ChannelFuture异步对象，通过isDone()等方法可以判断异步事件的执行情况
            // 启动服务器(并绑定端口),bind 是异步操作 ，sync 方法是等待异步操作执行完毕
            ChannelFuture cf = bootstrap.bind(9000).sync();
            // 给cf注册监听器，监听我们关心的事件
            cf.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        System.out.println("监听端口9000成功");
                    } else {
                        System.out.println("监听端口9000失败");
                    }
                }
            });
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
