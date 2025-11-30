package com.itheima.bigevent.service;

import com.itheima.bigevent.pojo.Message;
import com.itheima.bigevent.pojo.PageBean;

public interface MessageService {

    void add(Message message);

    PageBean<Message> list(Integer pageNum, Integer pageSize);

    void like(Integer id);

    void delete(Integer id);
}

