package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.AlbumImage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AlbumImageMapper {
    @Insert("insert into album_image(album_id, image_url, image_name, image_desc, sort_order, create_time) " +
            "values(#{albumId}, #{imageUrl}, #{imageName}, #{imageDesc}, #{sortOrder}, now())")
    void add(AlbumImage image);

    @Select("select * from album_image where album_id = #{albumId} order by sort_order, create_time")
    List<AlbumImage> findByAlbumId(Integer albumId);

    @Delete("delete from album_image where album_id = #{albumId}")
    void deleteByAlbumId(Integer albumId);

    @Delete("delete from album_image where id = #{id}")
    void delete(Integer id);
}

