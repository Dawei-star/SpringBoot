package com.itheima.bigevent.service.impl;

import com.itheima.bigevent.mapper.ArticleLikeMapper;
import com.itheima.bigevent.mapper.ArticleMapper;
import com.itheima.bigevent.service.ArticleLikeService;
import com.itheima.bigevent.utils.HttpUtil;
import com.itheima.bigevent.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文章点赞服务实现
 */
@Service
public class ArticleLikeServiceImpl implements ArticleLikeService {

  @Autowired
  private ArticleLikeMapper articleLikeMapper;

  @Autowired
  private ArticleMapper articleMapper;

  @Override
  public void like(Integer articleId) {
    // 获取用户ID（如果已登录）
    Integer userId = getCurrentUserId();

    // 获取IP地址
    String ip = HttpUtil.getClientIp();

    // 检查是否已点赞
    if (articleLikeMapper.exists(articleId, userId, ip)) {
      throw new RuntimeException("您已经点赞过了");
    }

    // 添加点赞记录
    articleLikeMapper.insert(articleId, userId, ip);

    // 更新文章点赞数
    articleMapper.incrementLikeCount(articleId);
  }

  @Override
  public void unlike(Integer articleId) {
    Integer userId = getCurrentUserId();
    String ip = HttpUtil.getClientIp();

    // 删除点赞记录
    articleLikeMapper.delete(articleId, userId, ip);

    // 更新文章点赞数
    articleMapper.decrementLikeCount(articleId);
  }

  @Override
  public Boolean isLiked(Integer articleId) {
    Integer userId = getCurrentUserId();
    String ip = HttpUtil.getClientIp();
    return articleLikeMapper.exists(articleId, userId, ip);
  }

  @Override
  public Integer getLikeCount(Integer articleId) {
    return articleLikeMapper.countByArticleId(articleId);
  }

  /**
   * 获取当前用户ID（可能为null，如果未登录）
   */
  private Integer getCurrentUserId() {
    return ThreadLocalUtil.getCurrentUserId();
  }
}
