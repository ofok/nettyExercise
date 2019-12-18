package kit.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import kit.netty.service.NettyServerHandler;


public class NettyClient {
    public static void main(String[] args) {
        // 客户端需要一个事件循环组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // 创建客户端启动对象
            // 注意客户端使用的不是ServerBootstrap 而是 bootStrap
            Bootstrap bootstrap = new Bootstrap();
            // 设置相关参数
            // 设置线程组
            bootstrap.group(group)
                    // 使用NioSocketChannel 作为客户端的通道实现
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 加入处理器
                            socketChannel.pipeline().addLast(new NettyClientHandler());
                        }
                    });
            System.out.println("netty client start...");
            // 启动客户端去链接服务器
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 9000).sync();
            // 对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
