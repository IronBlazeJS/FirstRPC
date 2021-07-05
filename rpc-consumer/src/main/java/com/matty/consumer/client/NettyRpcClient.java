package com.matty.consumer.client;

import com.matty.consumer.handler.NettyRpcClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * ClassName: NettyRpcClient
 * author: Matty Roslak
 * date: 2021/7/4  20:48
 * netty客户端
 * 1. 连接服务端
 * 2. 关闭资源
 * 3. 提供发送消息的方法
 */
@Component
public class NettyRpcClient implements InitializingBean, DisposableBean {

    EventLoopGroup group = null;
    Channel channel = null;

    @Autowired
    NettyRpcClientHandler nettyRpcClientHandler;

    // 声明线程池
    ExecutorService service = Executors.newCachedThreadPool();

    /**
     * 1. 连接服务端
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            // 1.1 创建线程组
            group = new NioEventLoopGroup();
            // 1.2 创建客户端启动助手
            Bootstrap bootstrap = new Bootstrap();
            // 1.3 设置参数
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 添加String类型编解码器
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new StringEncoder());

                            socketChannel.pipeline().addLast(nettyRpcClientHandler);
                        }
                    });
            // 1.4 连接服务
            channel = bootstrap.connect("127.0.0.1", 8899).sync().channel();
        } catch (Exception e) {
            e.printStackTrace();
            // 2. 关闭资源
            if(channel != null) {
                channel.close();
            }
            if(group != null) {
                group.shutdownGracefully();
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        // Spring容器关闭后关闭资源
        if(channel != null) {
            channel.close();
        }
        if(group != null) {
            group.shutdownGracefully();
        }
    }

    /**
     * 3. 消息发送
     * @param msg
     * @return
     */
    public Object send(String msg) throws ExecutionException, InterruptedException {
        // 设置发送的消息
        nettyRpcClientHandler.setReqMsg(msg);
        // 通过线程池发送并获取结果
        Future submit = service.submit(nettyRpcClientHandler);
        return submit.get();
    }
}
