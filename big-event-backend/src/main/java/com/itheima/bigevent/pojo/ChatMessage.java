package com.itheima.bigevent.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Data
public class ChatMessage {
  private Integer id;
  private Integer userId;
  private String username;
  private String content;
  private String reply; // 系统回复
  private String sender; // user 或 system
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTime;
}
