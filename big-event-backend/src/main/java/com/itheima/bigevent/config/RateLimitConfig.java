package com.itheima.bigevent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * 接口限流配置
 * 使用Redis实现分布式限流
 */
@Configuration
public class RateLimitConfig {

    /**
     * 限流脚本（滑动窗口算法）
     */
    @Bean
    public DefaultRedisScript<Long> rateLimitScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(
            "local key = KEYS[1]\n" +
            "local window = tonumber(ARGV[1])\n" +
            "local limit = tonumber(ARGV[2])\n" +
            "local now = tonumber(ARGV[3])\n" +
            "local clearBefore = now - window\n" +
            "\n" +
            "redis.call('zremrangebyscore', key, 0, clearBefore)\n" +
            "local current = redis.call('zcard', key)\n" +
            "\n" +
            "if current < limit then\n" +
            "    redis.call('zadd', key, now, now)\n" +
            "    redis.call('expire', key, window)\n" +
            "    return 1\n" +
            "else\n" +
            "    return 0\n" +
            "end"
        );
        script.setResultType(Long.class);
        return script;
    }
}

