package com.itheima.bigevent.service;

import com.itheima.bigevent.pojo.Article;

import java.util.List;

/**
 * 缓存服务接口
 */
public interface CacheService {

    /**
     * 缓存文章详情
     */
    void cacheArticle(Article article);

    /**
     * 获取缓存的文章详情
     */
    Article getCachedArticle(Integer articleId);

    /**
     * 删除文章缓存
     */
    void evictArticleCache(Integer articleId);

    /**
     * 缓存热门文章列表
     */
    void cacheHotArticles(List<Article> articles);

    /**
     * 获取缓存的热门文章
     */
    List<Article> getCachedHotArticles();

    /**
     * 清除所有文章缓存
     */
    void evictAllArticleCache();
}

