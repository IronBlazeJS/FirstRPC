package com.matty.provider.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClassName: RpcService
 * author: Matty Roslak
 * date: 2021/7/4  18:35
 * 用于暴露服务接口
 */
@Target(ElementType.TYPE) // 用于类上
@Retention(RetentionPolicy.RUNTIME) // 在运行时可以获取到
public @interface RpcService {
}
