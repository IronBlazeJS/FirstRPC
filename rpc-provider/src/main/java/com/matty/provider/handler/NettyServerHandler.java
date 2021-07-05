package com.matty.provider.handler;

import com.alibaba.fastjson.JSON;
import com.matty.provider.anno.RpcService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.beans.BeansException;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import rpc.common.RpcRequest;
import rpc.common.RpcResponse;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * ClassName: NettyServerHandler
 * author: Matty Roslak
 * date: 2021/7/4  18:57
 * 自定义业务处理类
 * 1. 将标有@RpcService注解的bean进行缓存
 * 2. 接收客户端请求
 * 3. 根据传递过来的beanName从缓存中查找bean
 * 4. 通过反射调用bean的方法
 * 5. 响应客户端
 */
@Component
@ChannelHandler.Sharable  // 设置通道共享，因为这里交给Spring容器管理之后默认是单例模式，所以需要这个注解让这个处理类能够被多个客户端共享
public class NettyServerHandler extends SimpleChannelInboundHandler<String> implements ApplicationContextAware {

    // 创建缓存对象
    static Map<String, Object> SERVICE_INSTANCE_MAP = new HashMap<>();

    /**
     * 1. 将标有@RpcService注解的bean进行缓存
     *
     * @param applicationContext
     * @throws BeansException 之前的项目里面也有相关内容 先得到Spring容器对象，再获取其中带特定注解的bean
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 1.1 通过注解获取bean的集合 也就是那个接口的实现类
        Map<String, Object> servicesMap = applicationContext.getBeansWithAnnotation(RpcService.class);

        // 1.2 循环遍历 得到实现类对象
        Set<Map.Entry<String, Object>> entries = servicesMap.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
           Object serviceBean = entry.getValue();
           // 判断ServiceBean有没有实现公共接口 使用反射的知识获取Class对象
            if(serviceBean.getClass().getInterfaces().length == 0) {
                throw new RuntimeException("对外暴露的服务必须实现公共接口");
            }

            // 默认处理，第一个接口名字作为缓存bean的名字 进行缓存
            // 会不会重复？多个类的第一个接口名都是那个公共接口怎么办？还真会重复，这里就是这样处理了，之后能改的
            String serviceName = serviceBean.getClass().getInterfaces()[0].getName();
            SERVICE_INSTANCE_MAP.put(serviceName, serviceBean);
            System.out.println("cache = " + SERVICE_INSTANCE_MAP);
        }

    }

    /**
     * 通道读取就绪事件--读取客户端消息
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // 2. 接收客户端请求，也就是msg
        // 首先需要转换，将客户端发送的JSON字符串msg转换为RpcRequest对象
        RpcRequest rpcRequest = JSON.parseObject(msg, RpcRequest.class);

        // 设置响应对象
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());

        // 业务处理
        // 新建handler方法处理3,4两步内容
        try {
            rpcResponse.setResult(handler(rpcRequest));
        } catch (Exception e) {
            e.printStackTrace();
            rpcResponse.setError(e.getMessage());
        }

        // 5. 响应客户端
        ctx.writeAndFlush(JSON.toJSONString(rpcResponse));

    }

    private Object handler(RpcRequest rpcRequest) throws InvocationTargetException {
//        3. 根据传递过来的beanName从缓存中查找bean
        Object serviceBean = SERVICE_INSTANCE_MAP.get(rpcRequest.getClassName());
        System.out.println("serviceBean = " + serviceBean);
        if(serviceBean == null) {
            throw new RuntimeException("服务端没有找到服务");
        }

//        4. 通过反射调用bean的方法
        // 创建serviceBean对应的代理对象
        FastClass proxyClass = FastClass.create(serviceBean.getClass());
        FastMethod method = proxyClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
        // 调用方法，执行，获得返回值，返回
        return method.invoke(serviceBean, rpcRequest.getParameters());
    }

}
