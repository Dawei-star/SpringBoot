package com.itheima.bigevent.controller;

import com.itheima.bigevent.pojo.Comment;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.CommentService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
@Validated
public class CommentController {

  @Autowired
  private CommentService commentService;

  /**
   * 添加评论
   */
  @PostMapping
  public Result<String> add(@RequestBody @Validated Comment comment) {
    commentService.add(comment);
    return Result.success();
  }

  /**
   * 获取文章评论列表
   */
  @GetMapping
  public Result<List<Comment>> list(@NotNull @RequestParam("articleId") Integer articleId) {
    List<Comment> comments = commentService.getArticleComments(articleId);
    return Result.success(comments);
  }

  /**
   * 点赞评论
   */
  @PostMapping("/like")
  public Result<String> like(@NotNull @RequestParam("id") Integer id) {
    commentService.like(id);
    return Result.success();
  }

  /**
   * 删除评论
   */
  @DeleteMapping
  public Result<String> delete(@NotNull @RequestParam("id") Integer id) {
    commentService.delete(id);
    return Result.success();
  }

  /**
   * 获取文章评论数量
   */
  @GetMapping("/count")
  public Result<Integer> count(@NotNull @RequestParam("articleId") Integer articleId) {
    Integer count = commentService.getCommentCount(articleId);
    return Result.success(count);
  }
}
