package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.ArticleViewService;
import com.itheima.bigevent.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private ArticleViewService articleViewService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> data = statisticsService.getDashboardData();
        return Result.success(data);
    }

    /**
     * 记录文章访问量
     */
    @PostMapping("/view")
    public Result<Void> recordView(@RequestParam Integer articleId) {
        articleViewService.incrementView(articleId);
        return Result.success();
    }

    /**
     * 获取文章访问量
     */
    @GetMapping("/view")
    public Result<Integer> getViewCount(@RequestParam Integer articleId) {
        Integer count = articleViewService.getViewCount(articleId);
        return Result.success(count);
    }

    /**
     * 获取热门文章ID列表
     */
    @GetMapping("/hot")
    public Result<java.util.List<Integer>> getHotArticles(@RequestParam(defaultValue = "10") Integer limit) {
        java.util.List<Integer> hotArticleIds = articleViewService.getHotArticleIds(limit);
        return Result.success(hotArticleIds);
    }
}
