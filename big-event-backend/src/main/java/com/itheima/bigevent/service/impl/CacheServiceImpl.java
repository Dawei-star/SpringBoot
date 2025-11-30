package com.itheima.bigevent.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.itheima.bigevent.pojo.Article;
import com.itheima.bigevent.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CacheServiceImpl implements CacheService {

    private static final String ARTICLE_CACHE_PREFIX = "article:detail:";
    private static final String HOT_ARTICLES_KEY = "article:hot";
    private static final long CACHE_TTL_HOURS = 24; // 缓存24小时
    private static final long HOT_CACHE_TTL_MINUTES = 30; // 热门文章缓存30分钟

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    public CacheServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void cacheArticle(Article article) {
        if (article == null || article.getId() == null) return;
        try {
            String key = ARTICLE_CACHE_PREFIX + article.getId();
            String json = objectMapper.writeValueAsString(article);
            redisTemplate.opsForValue().set(key, json != null ? json : "", CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            // 缓存失败不影响主流程
        }
    }

    @Override
    public Article getCachedArticle(Integer articleId) {
        if (articleId == null) return null;
        try {
            String key = ARTICLE_CACHE_PREFIX + articleId;
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                return objectMapper.readValue(json, Article.class);
            }
        } catch (JsonProcessingException e) {
            // 解析失败返回null，从数据库获取
        }
        return null;
    }

    @Override
    public void evictArticleCache(Integer articleId) {
        if (articleId == null) return;
        String key = ARTICLE_CACHE_PREFIX + articleId;
        redisTemplate.delete(key);
    }

    @Override
    public void cacheHotArticles(List<Article> articles) {
        if (articles == null || articles.isEmpty()) return;
        try {
            String json = objectMapper.writeValueAsString(articles);
            redisTemplate.opsForValue().set(HOT_ARTICLES_KEY, json != null ? json : "", HOT_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            // 缓存失败不影响主流程
        }
    }

    @Override
    public List<Article> getCachedHotArticles() {
        try {
            String json = redisTemplate.opsForValue().get(HOT_ARTICLES_KEY);
            if (json != null) {
                return objectMapper.readValue(json, new TypeReference<List<Article>>() {});
            }
        } catch (JsonProcessingException e) {
            // 解析失败返回空列表
        }
        return Collections.emptyList();
    }

    @Override
    public void evictAllArticleCache() {
        // 删除所有文章详情缓存
        Set<String> keys = redisTemplate.keys(ARTICLE_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        // 删除热门文章缓存
        redisTemplate.delete(HOT_ARTICLES_KEY);
    }
}

