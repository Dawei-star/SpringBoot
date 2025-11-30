package com.itheima.bigevent.service;

import java.util.Map;

public interface ArticleViewService {

  /**
   * 增加文章访问量
   */
  void incrementView(Integer articleId);

  /**
   * 获取文章访问量
   */
  Integer getViewCount(Integer articleId);

  /**
   * 获取热门文章ID列表
   */
  java.util.List<Integer> getHotArticleIds(Integer limit);

  /**
   * 获取统计数据
   */
  Map<String, Object> getStatistics();
}
