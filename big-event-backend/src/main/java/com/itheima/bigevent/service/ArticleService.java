package com.itheima.bigevent.service;

import com.itheima.bigevent.pojo.Article;
import com.itheima.bigevent.pojo.PageBean;

public interface ArticleService {

    void add(final Article article);

    // 文章列表（支持关键词搜索）
    PageBean<Article> list(final Integer pageNum,
            final Integer pageSize,
            final Integer categoryId,
            final String state,
            final String keyword);

    // 公开搜索接口
    PageBean<Article> search(final Integer pageNum,
            final Integer pageSize,
            final String keyword,
            final Integer categoryId);

    Article findById(final Integer id);

    void update(final Article article);

    void delete(Integer id);
}
