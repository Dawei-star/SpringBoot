package com.itheima.bigevent.service;

import com.itheima.bigevent.pojo.OperationLog;
import com.itheima.bigevent.pojo.PageBean;

public interface OperationLogService {

    PageBean<OperationLog> list(Integer pageNum,
                                Integer pageSize,
                                String username,
                                String module,
                                String beginTime,
                                String endTime);
}


