package com.itheima.bigevent.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 文章点赞Mapper
 */
@Mapper
public interface ArticleLikeMapper {

  /**
   * 插入点赞记录
   */
  @Insert("INSERT INTO article_like(article_id, user_id, ip, create_time) " +
      "VALUES(#{articleId}, #{userId}, #{ip}, NOW())")
  void insert(Integer articleId, Integer userId, String ip);

  /**
   * 删除点赞记录
   */
  @Delete("DELETE FROM article_like WHERE article_id = #{articleId} " +
      "AND (user_id = #{userId} OR (user_id IS NULL AND ip = #{ip}))")
  void delete(Integer articleId, Integer userId, String ip);

  /**
   * 检查是否已点赞
   */
  @Select("SELECT COUNT(*) > 0 FROM article_like " +
      "WHERE article_id = #{articleId} " +
      "AND (user_id = #{userId} OR (user_id IS NULL AND ip = #{ip}))")
  Boolean exists(Integer articleId, Integer userId, String ip);

  /**
   * 统计文章点赞数
   */
  @Select("SELECT COUNT(*) FROM article_like WHERE article_id = #{articleId}")
  Integer countByArticleId(Integer articleId);
}
