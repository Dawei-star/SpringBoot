package com.itheima.bigevent.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论实体类
 */
@Data
public class Comment {

  private Integer id;

  @NotNull(message = "文章ID不能为空")
  private Integer articleId;

  // 评论用户ID（可为空，支持匿名评论）
  private Integer userId;

  // 评论者昵称
  @NotEmpty(message = "昵称不能为空")
  private String nickname;

  // 评论内容
  @NotEmpty(message = "评论内容不能为空")
  private String content;

  // 父评论ID（为空表示一级评论）
  private Integer parentId;

  // 回复的评论ID
  private Integer replyId;

  // 回复的用户昵称
  private String replyNickname;

  // 点赞数
  private Integer likeCount;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updateTime;

  // 子评论列表（非数据库字段）
  private List<Comment> children;
}
