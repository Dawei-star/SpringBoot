package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.ArticleLikeService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 文章点赞控制器
 */
@RestController
@RequestMapping("/article/like")
@Validated
public class ArticleLikeController {

  @Autowired
  private ArticleLikeService articleLikeService;

  /**
   * 点赞文章
   */
  @PostMapping
  public Result<String> like(@NotNull @RequestParam("articleId") Integer articleId) {
    articleLikeService.like(articleId);
    return Result.success("点赞成功");
  }

  /**
   * 取消点赞
   */
  @DeleteMapping
  public Result<String> unlike(@NotNull @RequestParam("articleId") Integer articleId) {
    articleLikeService.unlike(articleId);
    return Result.success("取消点赞成功");
  }

  /**
   * 获取点赞状态
   */
  @GetMapping("/status")
  public Result<Boolean> getStatus(@NotNull @RequestParam("articleId") Integer articleId) {
    Boolean isLiked = articleLikeService.isLiked(articleId);
    return Result.success(isLiked);
  }

  /**
   * 获取文章点赞数
   */
  @GetMapping("/count")
  public Result<Integer> getCount(@NotNull @RequestParam("articleId") Integer articleId) {
    Integer count = articleLikeService.getLikeCount(articleId);
    return Result.success(count);
  }
}
