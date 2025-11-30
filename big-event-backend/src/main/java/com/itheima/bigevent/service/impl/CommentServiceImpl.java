package com.itheima.bigevent.service.impl;

import com.itheima.bigevent.mapper.CommentMapper;
import com.itheima.bigevent.pojo.Comment;
import com.itheima.bigevent.service.CommentService;
import com.itheima.bigevent.utils.XssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

  @Autowired
  private CommentMapper commentMapper;

  @Override
  public void add(Comment comment) {
    // XSS 防护：清理用户输入
    comment.setNickname(XssUtil.escapeHtml(comment.getNickname()));
    comment.setContent(XssUtil.clean(comment.getContent()));

    // 如果是回复评论，设置父评论ID
    if (comment.getReplyId() != null) {
      Comment replyComment = commentMapper.findById(comment.getReplyId());
      if (replyComment != null) {
        comment.setReplyNickname(replyComment.getNickname());
        // 如果回复的是一级评论，父ID就是回复ID
        // 如果回复的是子评论，父ID是子评论的父ID（保持二级结构）
        if (replyComment.getParentId() == null) {
          comment.setParentId(replyComment.getId());
        } else {
          comment.setParentId(replyComment.getParentId());
        }
      }
    }
    commentMapper.add(comment);
  }

  @Override
  public List<Comment> getArticleComments(Integer articleId) {
    // 获取一级评论
    List<Comment> rootComments = commentMapper.findRootComments(articleId);

    // 为每个一级评论加载子评论
    for (Comment root : rootComments) {
      List<Comment> children = commentMapper.findChildComments(root.getId());
      root.setChildren(children != null ? children : new ArrayList<>());
    }

    return rootComments;
  }

  @Override
  public void like(Integer id) {
    commentMapper.addLike(id);
  }

  @Override
  public void delete(Integer id) {
    // 获取评论信息
    Comment comment = commentMapper.findById(id);
    if (comment != null) {
      // 如果是一级评论，同时删除其子评论
      if (comment.getParentId() == null) {
        List<Comment> children = commentMapper.findChildComments(id);
        for (Comment child : children) {
          commentMapper.delete(child.getId());
        }
      }
      commentMapper.delete(id);
    }
  }

  @Override
  public Integer getCommentCount(Integer articleId) {
    return commentMapper.countByArticleId(articleId);
  }
}
