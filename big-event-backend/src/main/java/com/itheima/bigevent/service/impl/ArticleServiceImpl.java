package com.itheima.bigevent.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.bigevent.mapper.ArticleMapper;
import com.itheima.bigevent.pojo.Article;
import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.service.ArticleService;
import com.itheima.bigevent.service.CacheService;
import com.itheima.bigevent.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CacheService cacheService;

    @Override
    public void add(final Article article) {
        article.setCreateUser(ThreadLocalUtil.getCurrentUserIdRequired());
        articleMapper.add(article);
    }

    @Override
    public PageBean<Article> list(final Integer pageNum, final Integer pageSize, final Integer categoryId,
            final String state, final String keyword) {
        final PageBean<Article> pageBean = new PageBean<>();
        final int pn = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        final int ps = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        PageHelper.startPage(pn, ps);
        final Integer id = ThreadLocalUtil.getCurrentUserId();
        final String effectiveState = (id == null && (state == null || state.isBlank())) ? "已发布" : state;
        final Page<Article> articleList = (Page<Article>) articleMapper.list(id, categoryId, effectiveState, keyword);
        pageBean.setTotal(articleList.getTotal());
        pageBean.setItems(articleList.getResult());
        return pageBean;
    }

    @Override
    public PageBean<Article> search(final Integer pageNum, final Integer pageSize, final String keyword,
            final Integer categoryId) {
        final PageBean<Article> pageBean = new PageBean<>();
        final int pn = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        final int ps = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        PageHelper.startPage(pn, ps);
        final Page<Article> articleList = (Page<Article>) articleMapper.search(keyword, categoryId);
        pageBean.setTotal(articleList.getTotal());
        pageBean.setItems(articleList.getResult());
        return pageBean;
    }

    @Override
    public Article findById(final Integer id) {
        // 先从缓存获取
        Article cached = cacheService.getCachedArticle(id);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，从数据库获取
        Article article = articleMapper.findById(id);

        // 缓存文章
        if (article != null) {
            cacheService.cacheArticle(article);
        }

        return article;
    }

    @Override
    public void update(final Article article) {
        articleMapper.update(article);
        // 更新后清除缓存
        cacheService.evictArticleCache(article.getId());
    }

    @Override
    public void delete(final Integer id) {
        articleMapper.delete(id);
        // 删除后清除缓存
        cacheService.evictArticleCache(id);
    }
}
