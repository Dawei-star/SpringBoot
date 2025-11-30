package com.itheima.bigevent.pojo;

/**
 * 统一状态码枚举
 */
public enum ResultCode {
    // 成功状态码 (200-299)
    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    UPDATED(202, "更新成功"),
    DELETED(203, "删除成功"),
    LOGIN_SUCCESS(210, "登录成功"),
    LOGOUT_SUCCESS(211, "退出成功"),
    REGISTER_SUCCESS(212, "注册成功"),
    
    // 客户端错误 (400-499)
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),
    VALIDATION_ERROR(422, "参数校验失败"),
    
    // 业务错误 (1000-1999)
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USERNAME_TAKEN(1003, "用户名已被占用"),
    PASSWORD_ERROR(1004, "密码错误"),
    PASSWORD_MISMATCH(1005, "两次密码不一致"),
    TOKEN_EXPIRED(1006, "Token已过期"),
    TOKEN_INVALID(1007, "Token无效"),
    TOKEN_MISSING(1008, "Token缺失"),
    LOGIN_REQUIRED(1009, "请先登录"),
    PERMISSION_DENIED(1010, "权限不足"),
    OPERATION_FAILED(1011, "操作失败"),
    DATA_NOT_FOUND(1012, "数据不存在"),
    DATA_ALREADY_EXISTS(1013, "数据已存在"),
    INVALID_PARAMETER(1014, "参数无效"),
    RATE_LIMIT_EXCEEDED(1015, "请求过于频繁，请稍后再试"),
    
    // 服务器错误 (500-599)
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),
    
    // 数据库错误 (2000-2999)
    DATABASE_ERROR(2001, "数据库操作失败"),
    DATABASE_CONNECTION_ERROR(2002, "数据库连接失败"),
    
    // Redis错误 (3000-3999)
    REDIS_ERROR(3001, "缓存服务异常"),
    REDIS_CONNECTION_ERROR(3002, "缓存连接失败");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}

