package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.OperationLog;
import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/operation/log")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/list")
    public Result<PageBean<OperationLog>> list(@RequestParam(defaultValue = "1") final Integer pageNum,
                                               @RequestParam(defaultValue = "10") final Integer pageSize,
                                               final String username,
                                               final String module,
                                               final String beginTime,
                                               final String endTime) {
        PageBean<OperationLog> pageBean = operationLogService.list(pageNum, pageSize, username, module, beginTime, endTime);
        return Result.success(pageBean);
    }
}


