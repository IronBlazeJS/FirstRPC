package rpc.common;

import lombok.Data;

/**
 * ClassName: RpcRequest
 * author: Matty Roslak
 * date: 2021/7/4  17:16
 * 封装的请求对象
 */
@Data
public class RpcRequest {

    /**
     * 请求对象的ID
     */
    private String requestId;

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 参数类型 int String什么的
     */
    private Class<?>[] parameterTypes;

    /**
     * 传入参数
     * 具体的参数值
     */
    private Object[] parameters;
}
