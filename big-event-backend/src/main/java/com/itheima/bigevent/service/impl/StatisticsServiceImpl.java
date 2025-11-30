package com.itheima.bigevent.service.impl;

import com.itheima.bigevent.mapper.ArticleMapper;
import com.itheima.bigevent.mapper.CategoryMapper;
import com.itheima.bigevent.mapper.MessageMapper;
import com.itheima.bigevent.mapper.UserMapper;
import com.itheima.bigevent.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

  @Autowired
  private ArticleMapper articleMapper;

  @Autowired
  private CategoryMapper categoryMapper;

  @Autowired
  private MessageMapper messageMapper;

  @Autowired
  private UserMapper userMapper;

  @Override
  public Map<String, Object> getDashboardData() {
    Map<String, Object> data = new HashMap<>();

    // 文章统计
    Long articleCount = articleMapper.count();
    Long publishedArticleCount = articleMapper.countByState("已发布");
    Long draftArticleCount = articleMapper.countByState("草稿");

    // 分类统计
    Long categoryCount = categoryMapper.count();

    // 留言统计
    Long messageCount = messageMapper.count();

    // 用户统计
    Long userCount = userMapper.count();
    Long adminCount = userMapper.countByRole("admin");

    // 最近7天文章发布趋势（简化版，返回总数）
    Map<String, Long> articleTrend = new HashMap<>();
    articleTrend.put("total", publishedArticleCount);

    data.put("articleCount", articleCount);
    data.put("publishedArticleCount", publishedArticleCount);
    data.put("draftArticleCount", draftArticleCount);
    data.put("categoryCount", categoryCount);
    data.put("messageCount", messageCount);
    data.put("userCount", userCount);
    data.put("adminCount", adminCount);
    data.put("articleTrend", articleTrend);

    return data;
  }
}
