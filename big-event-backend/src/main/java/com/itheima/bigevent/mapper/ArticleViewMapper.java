package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.ArticleView;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleViewMapper {

  @Select("SELECT * FROM article_view WHERE article_id = #{articleId}")
  ArticleView findByArticleId(Integer articleId);

  @Insert("INSERT INTO article_view(article_id, view_count, create_time, update_time) VALUES(#{articleId}, 1, now(), now())")
  void insert(Integer articleId);

  @Update("UPDATE article_view SET view_count = view_count + 1, update_time = now() WHERE article_id = #{articleId}")
  void incrementView(Integer articleId);

  @Select("SELECT * FROM article_view ORDER BY view_count DESC LIMIT #{limit}")
  List<ArticleView> findTopViewed(Integer limit);

  @Select("SELECT COALESCE(SUM(view_count), 0) FROM article_view")
  Long getTotalViews();

  @Delete("DELETE FROM article_view WHERE article_id = #{articleId}")
  void deleteByArticleId(Integer articleId);
}
