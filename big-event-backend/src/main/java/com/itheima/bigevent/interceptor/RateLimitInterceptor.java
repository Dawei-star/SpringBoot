package com.itheima.bigevent.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.pojo.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 速率限制拦截器
 * 防止暴力破解和DoS攻击
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

  private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);
  private final ObjectMapper objectMapper = new ObjectMapper();

  // 存储每个IP的请求计数和时间戳
  private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();

  // 速率限制配置：每分钟最多5次请求
  private static final int MAX_REQUESTS_PER_MINUTE = 5;
  private static final long TIME_WINDOW_MS = 60 * 1000; // 1分钟

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler) {
    // 只对登录和注册接口限流
    String uri = request.getRequestURI();
    if (!uri.equals("/user/login") && !uri.equals("/user/register")) {
      return true;
    }

    String clientIp = getClientIp(request);
    RateLimitInfo info = rateLimitMap.computeIfAbsent(clientIp, k -> new RateLimitInfo());

    long currentTime = System.currentTimeMillis();

    // 如果时间窗口已过期，重置计数
    if (currentTime - info.getWindowStart() > TIME_WINDOW_MS) {
      info.reset(currentTime);
    }

    // 检查是否超过限制
    int currentCount = info.incrementAndGet();
    if (currentCount > MAX_REQUESTS_PER_MINUTE) {
      log.warn("[速率限制] IP {} 请求过于频繁: {} 次/分钟, URI: {}",
          clientIp, currentCount, uri);
      writeErrorResponse(response, "请求过于频繁，请稍后再试（每分钟最多" + MAX_REQUESTS_PER_MINUTE + "次）");
      return false;
    }

    return true;
  }

  /**
   * 获取客户端真实IP
   */
  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    // 处理多个IP的情况（X-Forwarded-For可能包含多个IP）
    if (ip != null && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }
    return ip;
  }

  /**
   * 写入错误响应
   */
  private void writeErrorResponse(HttpServletResponse response, String message) {
    try {
      response.setStatus(429); // Too Many Requests
      response.setContentType("application/json;charset=UTF-8");
      response.setCharacterEncoding("UTF-8");

      Result<?> result = Result.error(ResultCode.INVALID_PARAMETER, message);
      String json = objectMapper.writeValueAsString(result);

      PrintWriter writer = response.getWriter();
      writer.write(json);
      writer.flush();
    } catch (Exception e) {
      log.error("写入速率限制错误响应失败", e);
    }
  }

  /**
   * 速率限制信息
   */
  private static class RateLimitInfo {
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());

    public int incrementAndGet() {
      return requestCount.incrementAndGet();
    }

    public long getWindowStart() {
      return windowStart.get();
    }

    public void reset(long newStartTime) {
      requestCount.set(0);
      windowStart.set(newStartTime);
    }
  }
}
