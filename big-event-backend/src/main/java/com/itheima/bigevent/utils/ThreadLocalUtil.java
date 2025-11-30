package com.itheima.bigevent.utils;

import java.util.Map;

/**
 * ThreadLocal 工具类
 */
@SuppressWarnings("all")
public class ThreadLocalUtil {
    //提供ThreadLocal对象,
    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();

    //根据键获取值
    public static <T> T get() {
        return (T) THREAD_LOCAL.get();
    }

    //存储键值对
    public static void set(Object value) {
        THREAD_LOCAL.set(value);
    }

    //清除ThreadLocal 防止内存泄漏
    public static void remove() {
        THREAD_LOCAL.remove();
    }

    /**
     * 获取当前登录用户ID
     * @return 用户ID，如果未登录则返回null
     */
    public static Integer getCurrentUserId() {
        Map<String, Object> map = get();
        if (map == null) {
            return null;
        }
        return (Integer) map.get("id");
    }

    /**
     * 获取当前登录用户ID（必须登录）
     * @return 用户ID
     * @throws RuntimeException 如果用户未登录或登录已过期
     */
    public static Integer getCurrentUserIdRequired() {
        Map<String, Object> map = get();
        if (map == null) {
            throw new RuntimeException("用户未登录或登录已过期");
        }
        Integer userId = (Integer) map.get("id");
        if (userId == null) {
            throw new RuntimeException("无法获取用户ID");
        }
        return userId;
    }
}
