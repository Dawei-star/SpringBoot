package com.itheima.bigevent.controller;

import com.itheima.bigevent.anno.OpLog;
import com.itheima.bigevent.pojo.Message;
import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.pojo.ResultCode;
import com.itheima.bigevent.service.MessageService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message")
@Validated
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    @OpLog(module = "留言管理", operation = "新增留言")
    public Result<String> add(@RequestBody final Message message) {
        messageService.add(message);
        return Result.success(ResultCode.CREATED, "留言发布成功");
    }

    @GetMapping("/list")
    public Result<PageBean<Message>> list(@RequestParam(defaultValue = "1") final Integer pageNum,
                                          @RequestParam(defaultValue = "20") final Integer pageSize) {
        final PageBean<Message> pageBean = messageService.list(pageNum, pageSize);
        return Result.success(pageBean);
    }

    @PostMapping("/like")
    @OpLog(module = "留言管理", operation = "点赞留言")
    public Result<String> like(@NotNull @RequestParam("id") final Integer id) {
        messageService.like(id);
        return Result.success(ResultCode.SUCCESS, "点赞成功");
    }

    @DeleteMapping
    @OpLog(module = "留言管理", operation = "删除留言")
    public Result<String> delete(@NotNull @RequestParam("id") final Integer id) {
        messageService.delete(id);
        return Result.success(ResultCode.DELETED, "留言删除成功");
    }
}
