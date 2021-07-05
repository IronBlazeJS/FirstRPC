package com.matty.consumer.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClassName: RpcRefrence
 * author: Matty Roslak
 * date: 2021/7/4  22:13
 * 引用代理类
 */
@Target(ElementType.FIELD) // 作用于字段
@Retention(RetentionPolicy.RUNTIME) // 运行时能够获取到
public @interface RpcReference {
}
