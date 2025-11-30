package com.itheima.bigevent.service;

/**
 * 文章点赞服务接口
 */
public interface ArticleLikeService {

  /**
   * 点赞文章
   * 
   * @param articleId 文章ID
   */
  void like(Integer articleId);

  /**
   * 取消点赞
   * 
   * @param articleId 文章ID
   */
  void unlike(Integer articleId);

  /**
   * 检查是否已点赞
   * 
   * @param articleId 文章ID
   * @return 是否已点赞
   */
  Boolean isLiked(Integer articleId);

  /**
   * 获取文章点赞数
   * 
   * @param articleId 文章ID
   * @return 点赞数
   */
  Integer getLikeCount(Integer articleId);
}
