package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

  @Insert("INSERT INTO comment(article_id, user_id, nickname, content, parent_id, reply_id, reply_nickname, like_count, create_time, update_time) "
      +
      "VALUES(#{articleId}, #{userId}, #{nickname}, #{content}, #{parentId}, #{replyId}, #{replyNickname}, 0, now(), now())")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void add(Comment comment);

  // 查询文章的一级评论
  @Select("SELECT * FROM comment WHERE article_id = #{articleId} AND parent_id IS NULL ORDER BY create_time DESC")
  List<Comment> findRootComments(Integer articleId);

  // 查询评论的子评论
  @Select("SELECT * FROM comment WHERE parent_id = #{parentId} ORDER BY create_time ASC")
  List<Comment> findChildComments(Integer parentId);

  // 查询文章的所有评论
  @Select("SELECT * FROM comment WHERE article_id = #{articleId} ORDER BY create_time DESC")
  List<Comment> findByArticleId(Integer articleId);

  @Select("SELECT * FROM comment WHERE id = #{id}")
  Comment findById(Integer id);

  @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{id}")
  void addLike(Integer id);

  @Delete("DELETE FROM comment WHERE id = #{id}")
  void delete(Integer id);

  // 删除文章的所有评论
  @Delete("DELETE FROM comment WHERE article_id = #{articleId}")
  void deleteByArticleId(Integer articleId);

  // 统计文章评论数
  @Select("SELECT COUNT(*) FROM comment WHERE article_id = #{articleId}")
  Integer countByArticleId(Integer articleId);
}
