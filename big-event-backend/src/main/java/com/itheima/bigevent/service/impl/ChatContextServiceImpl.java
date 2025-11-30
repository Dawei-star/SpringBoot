package com.itheima.bigevent.service.impl;

import com.itheima.bigevent.mapper.ChatMessageMapper;
import com.itheima.bigevent.pojo.ChatMessage;
import com.itheima.bigevent.service.ChatContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ChatContextServiceImpl implements ChatContextService {

  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  @Autowired
  private ChatMessageMapper chatMessageMapper;

  private static final String CONTEXT_KEY_PREFIX = "chat:context:";
  private static final int CONTEXT_EXPIRE_HOURS = 24; // 上下文保存24小时

  @Override
  public Map<String, Object> getContext(Integer userId) {
    String key = CONTEXT_KEY_PREFIX + userId;
    String contextJson = stringRedisTemplate.opsForValue().get(key);

    if (contextJson == null || contextJson.isEmpty()) {
      return new HashMap<>();
    }

    try {
      // 简单的JSON解析（实际可以使用Jackson）
      Map<String, Object> context = new HashMap<>();
      // 这里简化处理，实际应该使用JSON解析
      return context;
    } catch (Exception e) {
      return new HashMap<>();
    }
  }

  @Override
  public void updateContext(Integer userId, Map<String, Object> context) {
    String key = CONTEXT_KEY_PREFIX + userId;
    // 简化处理，实际应该序列化为JSON
    stringRedisTemplate.opsForValue().set(key, "context", CONTEXT_EXPIRE_HOURS, TimeUnit.HOURS);
  }

  @Override
  public void clearContext(Integer userId) {
    String key = CONTEXT_KEY_PREFIX + userId;
    stringRedisTemplate.delete(key);
  }

  @Override
  public List<String> getRecentMessages(Integer userId, int count) {
    List<ChatMessage> messages = chatMessageMapper.findByUserId(userId, count);
    return messages.stream()
        .map(ChatMessage::getContent)
        .collect(Collectors.toList());
  }
}
