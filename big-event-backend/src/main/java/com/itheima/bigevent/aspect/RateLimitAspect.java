package com.itheima.bigevent.aspect;

import com.itheima.bigevent.anno.RateLimit;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.pojo.ResultCode;
import com.itheima.bigevent.utils.HttpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

/**
 * 接口限流切面
 */
@Aspect
@Component
@Order(1)
public class RateLimitAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DefaultRedisScript<Long> rateLimitScript;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String ip = HttpUtil.getClientIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 生成限流key
        String key = String.format("%s:%s:%s:%s", rateLimit.key(), ip, method, uri);

        // 执行限流脚本
        @SuppressWarnings("null")
        Long result = redisTemplate.execute(
            rateLimitScript,
            Collections.singletonList(key),
            String.valueOf(rateLimit.window() * 1000), // 转换为毫秒
            String.valueOf(rateLimit.limit()),
            String.valueOf(System.currentTimeMillis())
        );

        if (result == null || result == 0) {
            // 限流触发
            return Result.error(ResultCode.RATE_LIMIT_EXCEEDED);
        }

        // 继续执行
        return joinPoint.proceed();
    }
}

