package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.ChatMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

  @Insert("INSERT INTO chat_message(user_id, username, content, reply, sender, create_time) " +
      "VALUES(#{userId}, #{username}, #{content}, #{reply}, #{sender}, now())")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void add(ChatMessage message);

  @Select("SELECT * FROM chat_message WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
  List<ChatMessage> findByUserId(@Param("userId") Integer userId, @Param("limit") Integer limit);

  @Select("SELECT * FROM chat_message WHERE user_id = #{userId} ORDER BY create_time DESC")
  List<ChatMessage> findAllByUserId(Integer userId);

  @Delete("DELETE FROM chat_message WHERE user_id = #{userId}")
  void deleteByUserId(Integer userId);
}
