package com.itheima.bigevent.service;

import com.itheima.bigevent.pojo.User;

public interface UserService {

    User findByUsername(final String username);

    void register(final String username, final String password);

    void update(final User user);

    void updateAvatar(final String url);

    void updatePwd(final String newPwd);

    java.util.List<User> findAllUsers(final boolean isAdmin);

    void updateRole(final Integer id, final String role);

    void updateByAdmin(final User user);
}
