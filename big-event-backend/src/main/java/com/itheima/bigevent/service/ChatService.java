package com.itheima.bigevent.service;

import com.itheima.bigevent.pojo.ChatMessage;

import java.util.List;

public interface ChatService {
  /**
   * 发送聊天消息
   */
  ChatMessage sendMessage(String content, Integer userId, String username);

  /**
   * 获取用户聊天历史
   */
  List<ChatMessage> getHistory(Integer userId, Integer limit);
}
