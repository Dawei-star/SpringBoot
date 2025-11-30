package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.Article;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ArticleMapper {

    @Insert("insert into article(title, content, cover_img, state, category_id, create_user, create_time, update_time) "
            +
            "values(#{title},#{content},#{coverImg},#{state},#{categoryId},#{createUser},now(),now())")
    void add(final Article article);

    // 文章列表查询（支持关键词搜索）
    List<Article> list(final Integer id, final Integer categoryId, final String state, final String keyword);

    // 公开搜索接口（只搜索已发布文章）
    List<Article> search(final String keyword, final Integer categoryId);

    @Select("select * from article where id=#{id}")
    Article findById(final Integer id);

    @Update("update article set title=#{title},content=#{content},cover_img=#{coverImg},state=#{state},category_id=#{categoryId},update_time=now() where id=#{id}")
    void update(final Article article);

    @Delete("delete from article where id=#{id}")
    void delete(final Integer id);

    /**
     * 增加点赞数
     */
    @Update("UPDATE article SET like_count = COALESCE(like_count, 0) + 1 WHERE id = #{id}")
    void incrementLikeCount(Integer id);

    /**
     * 减少点赞数
     */
    @Update("UPDATE article SET like_count = GREATEST(COALESCE(like_count, 0) - 1, 0) WHERE id = #{id}")
    void decrementLikeCount(Integer id);

    @Select("SELECT COUNT(*) FROM article")
    Long count();

    @Select("SELECT COUNT(*) FROM article WHERE state = #{state}")
    Long countByState(String state);
}
