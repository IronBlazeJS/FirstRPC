package rpc.common;

import lombok.Data;

/**
 * ClassName: RpcResponse
 * author: Matty Roslak
 * date: 2021/7/4  17:16
 * 封装的响应对象
 */
@Data
public class RpcResponse {

    /**
     * 响应ID
     */
    private String requestId;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 返回的结果
     */
    private Object result;
}
