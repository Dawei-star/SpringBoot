package com.itheima.bigevent.service.impl;

import com.itheima.bigevent.mapper.UserMapper;
import com.itheima.bigevent.pojo.User;
import com.itheima.bigevent.service.UserService;
import com.itheima.bigevent.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User findByUsername(final String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public void register(final String username, final String password) {
        final String encodedPassword = passwordEncoder.encode(password);
        userMapper.add(username, encodedPassword);
    }

    @Override
    public void update(final User user) {
        userMapper.update(user);
    }

    @Override
    public void updateAvatar(final String url) {
        userMapper.updateAvatar(url, ThreadLocalUtil.getCurrentUserIdRequired());
    }

    @Override
    public void updatePwd(final String newPwd) {
        final String encodedPassword = passwordEncoder.encode(newPwd);
        userMapper.updatePwd(encodedPassword, ThreadLocalUtil.getCurrentUserIdRequired());
    }

    @Override
    public java.util.List<User> findAllUsers(final boolean isAdmin) {
        if (isAdmin) {
            return userMapper.findAllUsersForAdmin();
        } else {
            return userMapper.findAllUsers();
        }
    }

    @Override
    public void updateRole(final Integer id, final String role) {
        userMapper.updateRole(id, role);
    }

    @Override
    public void updateByAdmin(final User user) {
        userMapper.updateByAdmin(user);
    }
}
