package rpc.api;

import rpc.pojo.User;

/**
 * ClassName: IUserService
 * author: Matty Roslak
 * date: 2021/7/4  17:10
 */
public interface IUserService {
    User getById(int id);
}
