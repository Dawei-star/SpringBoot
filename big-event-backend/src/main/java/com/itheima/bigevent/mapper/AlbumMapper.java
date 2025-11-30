package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.Album;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AlbumMapper {
    @Insert("insert into album(title, description, cover_img, create_user, create_time, update_time) " +
            "values(#{title}, #{description}, #{coverImg}, #{createUser}, now(), now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void add(Album album);

    @Select("select * from album order by create_time desc")
    List<Album> list();

    @Select("select * from album where id = #{id}")
    Album findById(Integer id);

    @Update("update album set title=#{title}, description=#{description}, cover_img=#{coverImg}, update_time=now() where id=#{id}")
    void update(Album album);

    @Delete("delete from album where id = #{id}")
    void delete(Integer id);
}

