package com.itheima.bigevent.service;

import java.util.Map;

/**
 * 聊天上下文服务
 * 用于存储和管理用户的聊天上下文，支持多轮对话
 */
public interface ChatContextService {
  /**
   * 获取用户聊天上下文
   */
  Map<String, Object> getContext(Integer userId);

  /**
   * 更新用户聊天上下文
   */
  void updateContext(Integer userId, Map<String, Object> context);

  /**
   * 清除用户聊天上下文
   */
  void clearContext(Integer userId);

  /**
   * 获取用户最近的消息历史（用于上下文理解）
   */
  java.util.List<String> getRecentMessages(Integer userId, int count);
}
