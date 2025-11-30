package com.itheima.bigevent.anno;

import java.lang.annotation.*;

/**
 * 标记需要记录操作日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpLog {

    /**
     * 模块名称，例如：用户管理 / 文章管理
     */
    String module() default "";

    /**
     * 操作名称，例如：新增用户 / 修改文章
     */
    String operation() default "";
}


