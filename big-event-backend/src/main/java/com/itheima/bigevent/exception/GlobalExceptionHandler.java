package com.itheima.bigevent.exception;

import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.pojo.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.apache.catalina.connector.ClientAbortException;

import java.io.IOException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.error(ResultCode.VALIDATION_ERROR, message);
    }

    /**
     * 绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return Result.error(ResultCode.VALIDATION_ERROR, message);
    }

    /**
     * 参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不匹配: {}", e.getMessage());
        Class<?> requiredType = e.getRequiredType();
        String typeName = (requiredType != null) ? requiredType.getSimpleName() : "未知类型";
        return Result.error(ResultCode.VALIDATION_ERROR, "参数类型错误: " + e.getName() + " 应为 " + typeName);
    }

    /**
     * Redis连接异常
     */
    @ExceptionHandler(RedisConnectionFailureException.class)
    public Result<?> handleRedisException(RedisConnectionFailureException e) {
        log.error("Redis连接失败", e);
        return Result.error(ResultCode.REDIS_CONNECTION_ERROR);
    }

    /**
     * 数据库访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    public Result<?> handleDataAccessException(DataAccessException e) {
        log.error("数据库访问异常", e);
        return Result.error(ResultCode.DATABASE_ERROR);
    }

    /**
     * 客户端断开连接异常（静默处理，不记录为错误）
     * 通常发生在用户刷新页面、关闭浏览器或网络中断时
     */
    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException e) {
        // 只记录调试信息，不记录为错误
        // 使用void返回类型，避免Spring尝试写入响应
        log.debug("客户端断开连接: {}", e.getMessage());
    }

    /**
     * IO异常（可能是客户端断开连接）
     */
    @ExceptionHandler(IOException.class)
    public Result<?> handleIOException(IOException e) {
        if (isClientAbortException(e)) {
            return null; // 静默处理
        }
        log.warn("IO异常: {}", e.getMessage());
        return Result.error(ResultCode.SERVICE_UNAVAILABLE, "网络异常，请稍后重试");
    }

    /**
     * 运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        if (isClientAbortException(e)) {
            return null; // 静默处理
        }

        log.error("运行时异常", e);
        // 如果是业务异常，返回具体错误信息
        String message = e.getMessage();
        if (message != null && !message.isEmpty()) {
            return Result.error(ResultCode.OPERATION_FAILED, message);
        }
        return Result.error(ResultCode.OPERATION_FAILED);
    }

    /**
     * 兜底异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        if (isClientAbortException(e)) {
            return null; // 静默处理
        }

        log.error("系统异常", e);
        return Result.error(ResultCode.INTERNAL_SERVER_ERROR, "系统异常，请联系管理员");
    }

    /**
     * 判断是否是客户端断开连接异常
     */
    private boolean isClientAbortException(Throwable e) {
        if (e instanceof ClientAbortException) {
            log.debug("客户端断开连接: {}", e.getMessage());
            return true;
        }

        if (e instanceof IOException) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("连接") || msg.contains("connection")
                    || msg.contains("abort") || msg.contains("reset")
                    || msg.contains("中止") || msg.contains("软件"))) {
                log.debug("客户端连接异常: {}", msg);
                return true;
            }
        }

        // 检查异常原因
        Throwable cause = e.getCause();
        if (cause != null) {
            return isClientAbortException(cause);
        }

        return false;
    }
}
