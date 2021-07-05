package com.matty.provider.service;

import com.matty.provider.anno.RpcService;
import org.springframework.stereotype.Service;
import rpc.api.IUserService;
import rpc.pojo.User;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: UserServiceImpl
 * author: Matty Roslak
 * date: 2021/7/4  17:40
 * 接口实现类
 */
@RpcService
@Service
public class UserServiceImpl implements IUserService {
    Map<Object, User> userMap = new HashMap<>();

    @Override
    public User getById(int id) {
        if(userMap.size() == 0) {
            User user1 = new User();
            user1.setId(1);
            user1.setName("Matty");

            User user2 = new User();
            user2.setId(2);
            user2.setName("Lance");

            userMap.put(user1.getId(), user1);
            userMap.put(user2.getId(), user2);

        }
        return userMap.get(id);
    }
}
