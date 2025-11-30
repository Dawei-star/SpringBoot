package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.Message;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MessageMapper {

    @Insert("insert into message(nickname, content, like_count, create_time) values(#{nickname}, #{content}, 0, now())")
    void add(Message message);

    @Select("select * from message order by create_time desc")
    List<Message> list();

    @Update("update message set like_count = like_count + 1 where id = #{id}")
    void like(Integer id);

    @Delete("delete from message where id = #{id}")
    void delete(Integer id);

    @Select("SELECT COUNT(*) FROM message")
    Long count();
}
