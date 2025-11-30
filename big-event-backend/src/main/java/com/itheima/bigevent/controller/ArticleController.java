package com.itheima.bigevent.controller;

import com.itheima.bigevent.anno.OpLog;
import com.itheima.bigevent.pojo.Article;
import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.pojo.ResultCode;
import com.itheima.bigevent.service.ArticleService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/article")
@Validated
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @OpLog(module = "文章管理", operation = "新增文章")
    @PostMapping
    public Result<String> add(@RequestBody @Validated(Article.Add.class) Article article) {
        articleService.add(article);
        return Result.success(ResultCode.CREATED, "文章发布成功");
    }

    @GetMapping
    public Result<PageBean<Article>> list(@RequestParam(defaultValue = "1") final Integer pageNum,
            @RequestParam(defaultValue = "10") final Integer pageSize,
            @RequestParam(required = false) final Integer categoryId,
            @RequestParam(required = false) final String state,
            @RequestParam(required = false) final String keyword) {
        final PageBean<Article> articleList = articleService.list(pageNum, pageSize, categoryId, state, keyword);
        return Result.success(articleList);
    }

    /**
     * 公开搜索接口 - 搜索已发布的文章
     * 添加限流保护，防止恶意请求
     */
    @com.itheima.bigevent.anno.RateLimit(window = 60, limit = 100, key = "article_search")
    @GetMapping("/search")
    public Result<PageBean<Article>> search(@RequestParam(defaultValue = "1") final Integer pageNum,
            @RequestParam(defaultValue = "10") final Integer pageSize,
            @RequestParam(required = false) final String keyword,
            @RequestParam(required = false) final Integer categoryId) {
        final PageBean<Article> articleList = articleService.search(pageNum, pageSize, keyword, categoryId);
        return Result.success(articleList);
    }

    @GetMapping("/detail")
    public Result<Article> detail(@NotNull @RequestParam("id") final Integer id) {
        final Article article = articleService.findById(id);
        return Result.success(article);
    }

    @OpLog(module = "文章管理", operation = "修改文章")
    @PutMapping
    public Result<String> update(@RequestBody @Validated(Article.Update.class) Article article) {
        articleService.update(article);
        return Result.success(ResultCode.UPDATED, "文章更新成功");
    }

    @OpLog(module = "文章管理", operation = "删除文章")
    @DeleteMapping
    public Result<String> delete(@NotNull @RequestParam("id") final Integer id) {
        articleService.delete(id);
        return Result.success(ResultCode.DELETED, "文章删除成功");
    }
}
