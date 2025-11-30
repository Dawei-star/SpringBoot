package com.itheima.bigevent.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * 时间窗口（秒）
     */
    int window() default 60;
    
    /**
     * 限制次数
     */
    int limit() default 100;
    
    /**
     * 限流key的前缀
     */
    String key() default "rate_limit";
}

