package com.itheima.bigevent.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.bigevent.mapper.MessageMapper;
import com.itheima.bigevent.pojo.Message;
import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.service.MessageService;
import com.itheima.bigevent.utils.XssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public void add(final Message message) {
        // XSS 防护：清理用户输入
        message.setNickname(XssUtil.escapeHtml(message.getNickname()));
        message.setContent(XssUtil.clean(message.getContent()));
        messageMapper.add(message);
    }

    @Override
    public PageBean<Message> list(final Integer pageNum, final Integer pageSize) {
        final PageBean<Message> pageBean = new PageBean<>();
        PageHelper.startPage(pageNum, pageSize);
        final Page<Message> page = (Page<Message>) messageMapper.list();
        pageBean.setTotal(page.getTotal());
        pageBean.setItems(page.getResult());
        return pageBean;
    }

    @Override
    public void like(final Integer id) {
        messageMapper.like(id);
    }

    @Override
    public void delete(final Integer id) {
        messageMapper.delete(id);
    }
}

