package com.matty.consumer.controller;

import com.matty.consumer.anno.RpcReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import rpc.api.IUserService;
import rpc.pojo.User;

/**
 * ClassName: UserController
 * author: Matty Roslak
 * date: 2021/7/4  17:53
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @RpcReference
    IUserService userService;

    @RequestMapping(value = "/getUserById", produces="application/json; utf-8")
    public User getUserById(int id) {
        return userService.getById(id);
    }

}
