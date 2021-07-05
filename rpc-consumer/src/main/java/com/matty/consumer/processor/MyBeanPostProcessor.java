package com.matty.consumer.processor;

import com.matty.consumer.anno.RpcReference;
import com.matty.consumer.proxy.RpcClientProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * ClassName: MyBeanPostProcessor
 * author: Matty Roslak
 * date: 2021/7/4  22:19
 * bean的后置增强
 * 实现Spring的后置处理接口，实现后，每一个bean初始化后都会执行这个后置方法
 */
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    RpcClientProxy rpcClientProxy;

    /**
     * 自定义注解的注入
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        //1.查看bean的字段中有没有那个自定义注解
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            // 2. 查找字段中是否有注解
            RpcReference annotation = field.getAnnotation(RpcReference.class);
            if(annotation != null) {
                // 3. 获取代理对象
                Object proxy = rpcClientProxy.getProxy(field.getType());

                try {
                    // 4. 属性注入 注入代理对象
                    field.setAccessible(true); // 暴力破解
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return bean;
    }
}
