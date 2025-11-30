package com.itheima.bigevent.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.bigevent.mapper.OperationLogMapper;
import com.itheima.bigevent.pojo.OperationLog;
import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public PageBean<OperationLog> list(final Integer pageNum,
                                       final Integer pageSize,
                                       final String username,
                                       final String module,
                                       final String beginTime,
                                       final String endTime) {
        PageBean<OperationLog> pageBean = new PageBean<>();
        PageHelper.startPage(pageNum, pageSize);
        Page<OperationLog> page = (Page<OperationLog>) operationLogMapper.list(username, module, beginTime, endTime);
        pageBean.setTotal(page.getTotal());
        pageBean.setItems(page.getResult());
        return pageBean;
    }
}


