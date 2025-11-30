package com.itheima.bigevent.controller;

import com.itheima.bigevent.anno.OpLog;
import com.itheima.bigevent.pojo.ChatMessage;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.ChatService;
import com.itheima.bigevent.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@Validated
public class ChatController {

  @Autowired
  private ChatService chatService;

  @OpLog(module = "聊天", operation = "发送消息")
  @PostMapping("/send")
  public Result<ChatMessage> sendMessage(@RequestBody Map<String, String> params) {
    String content = params.get("content");
    if (content == null || content.trim().isEmpty()) {
      return Result.error("消息内容不能为空");
    }

    Map<String, Object> map = ThreadLocalUtil.get();
    Integer userId = (Integer) map.get("id");
    String username = (String) map.get("username");

    if (userId == null || username == null) {
      return Result.error("请先登录");
    }

    ChatMessage reply = chatService.sendMessage(content, userId, username);
    return Result.success(reply);
  }

  @OpLog(module = "聊天", operation = "获取聊天历史")
  @GetMapping("/history")
  public Result<List<ChatMessage>> getHistory(@RequestParam(defaultValue = "50") Integer limit) {
    Map<String, Object> map = ThreadLocalUtil.get();
    Integer userId = (Integer) map.get("id");

    if (userId == null) {
      return Result.error("请先登录");
    }

    List<ChatMessage> messages = chatService.getHistory(userId, limit);
    return Result.success(messages);
  }
}
