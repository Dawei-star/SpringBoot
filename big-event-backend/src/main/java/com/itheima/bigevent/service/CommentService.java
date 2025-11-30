package com.itheima.bigevent.service;

import com.itheima.bigevent.pojo.Comment;

import java.util.List;

public interface CommentService {

  /**
   * 添加评论
   */
  void add(Comment comment);

  /**
   * 获取文章的评论列表（树形结构）
   */
  List<Comment> getArticleComments(Integer articleId);

  /**
   * 点赞评论
   */
  void like(Integer id);

  /**
   * 删除评论
   */
  void delete(Integer id);

  /**
   * 获取文章评论数
   */
  Integer getCommentCount(Integer articleId);
}
