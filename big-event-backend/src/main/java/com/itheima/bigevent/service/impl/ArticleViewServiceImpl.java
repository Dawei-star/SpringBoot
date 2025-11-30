package com.itheima.bigevent.service.impl;

import com.itheima.bigevent.mapper.ArticleViewMapper;
import com.itheima.bigevent.pojo.ArticleView;
import com.itheima.bigevent.service.ArticleViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ArticleViewServiceImpl implements ArticleViewService {

    @Autowired
    private ArticleViewMapper articleViewMapper;

    @Override
    public void incrementView(Integer articleId) {
        ArticleView view = articleViewMapper.findByArticleId(articleId);
        if (view == null) {
            articleViewMapper.insert(articleId);
        } else {
            articleViewMapper.incrementView(articleId);
        }
    }

    @Override
    public Integer getViewCount(Integer articleId) {
        ArticleView view = articleViewMapper.findByArticleId(articleId);
        return view != null ? view.getViewCount() : 0;
    }

    @Override
    public List<Integer> getHotArticleIds(Integer limit) {
        List<ArticleView> topViewed = articleViewMapper.findTopViewed(limit);
        return topViewed.stream()
                .map(ArticleView::getArticleId)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总访问量
        Long totalViews = articleViewMapper.getTotalViews();
        stats.put("totalViews", totalViews);
        
        // 热门文章
        List<ArticleView> topViewed = articleViewMapper.findTopViewed(10);
        stats.put("hotArticles", topViewed);
        
        return stats;
    }
}

