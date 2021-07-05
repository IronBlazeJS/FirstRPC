package com.matty.consumer.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

/**
 * ClassName: NettyRpcClientHandler
 * author: Matty Roslak
 * date: 2021/7/4  21:09
 * 客户端业务处理类
 */
@Component
public class NettyRpcClientHandler extends SimpleChannelInboundHandler<String> implements Callable {
    ChannelHandlerContext context;

    private String reqMsg; // 发送消息

    private String respMsg; // 接收消息

    public void setReqMsg(String reqMsg) {
        this.reqMsg = reqMsg;
    }

    /**
     * 通道读取就绪事件--读取服务端消息
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected synchronized void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        respMsg = msg;
        // 唤醒等待线程
        notify();
    }

    /**
     * 通道连接就绪事件
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        context = ctx;
    }

    /**
     * 给服务端发送消息
     * @return
     * @throws Exception
     * call方法不能传参，所以再定义一个变量reqMsg，在call中直接将reqMsg发送出去
     */
    @Override
    public synchronized Object call() throws Exception {
        // 发送
        context.writeAndFlush(reqMsg);
        // 将线程处于等待状态
        wait();
        return respMsg;
    }
}
