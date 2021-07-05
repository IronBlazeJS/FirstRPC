package com.matty.consumer.proxy;

import com.alibaba.fastjson.JSON;
import com.matty.consumer.client.NettyRpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rpc.common.RpcRequest;
import rpc.common.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ClassName: RpcClientProxy
 * author: Matty Roslak
 * date: 2021/7/4  21:39
 * 客户端的代理类
 */
@Component
public class RpcClientProxy {

    @Autowired
    NettyRpcClient nettyRpcClient;

    // 缓存代理好的对象，方便其它对象复用
    Map<Class, Object> SERVICE_PROXY = new HashMap<>();

    /**
     * 获取代理对象
     * @return
     */
    public Object getProxy(Class serviceClass) {
        // 在缓存中查找
        Object proxy = SERVICE_PROXY.get(serviceClass);
        if(proxy == null) {
            // 创建代理对象  使用JDK自带动态代理
            proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(),
                    new Class[]{serviceClass}, new InvocationHandler() {
                // 增强，也就是如何封装？
                // 代理对象一调用方法时肯定会触发invoke方法
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    // 1. 封装请求对象
                    RpcRequest rpcRequest = new RpcRequest();
                    rpcRequest.setRequestId(UUID.randomUUID().toString());
                    // 通过反射获取方法对应类的名称
                    rpcRequest.setClassName(method.getDeclaringClass().getName());
                    System.out.println(rpcRequest.getClassName());
                    rpcRequest.setMethodName(method.getName());
                    rpcRequest.setParameterTypes(method.getParameterTypes());
                    rpcRequest.setParameters(args);

                    //2. 发送消息 调用client中的send
                    try {
                        Object msg = nettyRpcClient.send(JSON.toJSONString(rpcRequest));
                        // 3. 将服务端返回的JSON消息转化为RpcResponse对象
                        RpcResponse rpcResponse = JSON.parseObject(msg.toString(), RpcResponse.class);
                        if(rpcResponse.getError() != null) {
                            throw new RuntimeException(rpcResponse.getError());
                        }
                        // 返回服务器传回来的结果
                        if(rpcResponse.getResult() != null) {
                            return JSON.parseObject(rpcResponse.getResult().toString(), method.getReturnType());
                        }
                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
            });
            // 放入缓存
            SERVICE_PROXY.put(serviceClass, proxy);
            return proxy;
        } else {
            return proxy;
        }
    }

}
