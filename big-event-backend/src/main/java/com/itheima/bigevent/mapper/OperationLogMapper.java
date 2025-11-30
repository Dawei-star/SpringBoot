package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.OperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OperationLogMapper {

        @Insert("insert into operation_log(" +
                        "user_id, username, module, operation, request_method, class_method, request_uri, ip, " +
                        "request_params, result_code, result_message, error_message, create_time) " +
                        "values(#{userId}, #{username}, #{module}, #{operation}, #{requestMethod}, #{classMethod}, " +
                        "#{requestUri}, #{ip}, #{requestParams}, #{resultCode}, #{resultMessage}, #{errorMessage}, now())")
        void insert(OperationLog log);

        // 优化查询：只查询必要字段，使用索引
        @Select("select id, user_id, username, module, operation, request_method, class_method, " +
                        "request_uri, ip, request_params, result_code, result_message, error_message, create_time " +
                        "from operation_log " +
                        "where (#{username} is null or username = #{username}) " +
                        "and (#{module} is null or module = #{module}) " +
                        "and (#{beginTime} is null or create_time >= #{beginTime}) " +
                        "and (#{endTime} is null or create_time <= #{endTime}) " +
                        "order by create_time desc")
        java.util.List<OperationLog> list(@Param("username") String username,
                        @Param("module") String module,
                        @Param("beginTime") String beginTime,
                        @Param("endTime") String endTime);
}
