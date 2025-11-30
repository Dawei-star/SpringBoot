package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    @Insert("insert into user(username, password, create_time, update_time) " +
            "values(#{username},#{password},now(),now())")
    void add(final String username, final String password);

    @Select("select * from user where username=#{username}")
    User findByUsername(final String username);

    @Update("update user set nickname=#{nickname}, email=#{email}, update_time=now() where id=#{id}")
    void update(final User user);

    @Update("update user set user_pic=#{url}, update_time=now() where id=#{id}")
    void updateAvatar(final String url, final Integer id);

    @Update("update user set password=#{password}, update_time=now() where id=#{id}")
    void updatePwd(final String password, final Integer id);

    @Select("select * from user where role = 'user' order by create_time desc")
    java.util.List<User> findAllUsers();

    @Select("select * from user order by create_time desc")
    java.util.List<User> findAllUsersForAdmin();

    @Update("update user set role=#{role}, update_time=now() where id=#{id}")
    void updateRole(final Integer id, final String role);

    @Update("update user set nickname=#{nickname}, email=#{email}, role=#{role}, update_time=now() where id=#{id}")
    void updateByAdmin(final User user);

    @Select("SELECT COUNT(*) FROM user")
    Long count();

    @Select("SELECT COUNT(*) FROM user WHERE role = #{role}")
    Long countByRole(String role);
}
