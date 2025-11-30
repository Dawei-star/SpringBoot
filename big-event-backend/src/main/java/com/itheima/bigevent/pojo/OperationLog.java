package com.itheima.bigevent.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
public class OperationLog {

    private Long id;

    /** 用户ID */
    private Integer userId;

    /** 用户名 */
    private String username;

    /** 模块名称 */
    private String module;

    /** 操作名称 */
    private String operation;

    /** 请求方法 GET/POST... */
    private String requestMethod;

    /** 调用的方法签名 */
    private String classMethod;

    /** 请求路径 */
    private String requestUri;

    /** 请求 IP */
    private String ip;

    /** 请求参数（JSON 字符串） */
    private String requestParams;

    /** 响应结果编码（0 成功，非 0 失败） */
    private Integer resultCode;

    /** 响应消息 */
    private String resultMessage;

    /** 异常信息（如有） */
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}


