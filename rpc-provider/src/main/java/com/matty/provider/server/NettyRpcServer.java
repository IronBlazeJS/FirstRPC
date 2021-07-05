package com.matty.provider.server;

import com.matty.provider.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ClassName: NettyServer
 * author: Matty Roslak
 * date: 2021/7/4  18:40
 * Netty服务端
 * 启动服务端，监听端口
 */
@Component
public class NettyRpcServer implements DisposableBean {

    @Autowired
    NettyServerHandler nettyServerHandler;

    EventLoopGroup bossGroup = null;
    EventLoopGroup workerGroup = null;

    public void start(String host, int port) {
        try {
            // 1.创建BossGroup和WorkerGroup
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            // 2.设置启动助手
            ServerBootstrap bootstrap = new ServerBootstrap();

            // 3.设置启动参数
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 添加String JSON编解码器
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new StringEncoder());
                            // 添加自定义Handler
                            socketChannel.pipeline().addLast(nettyServerHandler);
                        }
                    });
            // 4. 绑定ip和端口
            ChannelFuture channelFuture = bootstrap.bind(host, port).sync();
            System.out.println("==========Netty服务端启动成功==========");

            // 5.监听通道关闭状态
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 出现异常关闭资源
            if(bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if(workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        // Spring容器关闭后关闭资源
        if(bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if(workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
