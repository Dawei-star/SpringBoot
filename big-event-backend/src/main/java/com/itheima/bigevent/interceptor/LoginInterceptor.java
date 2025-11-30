package com.itheima.bigevent.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.pojo.ResultCode;
import com.itheima.bigevent.utils.JwtUtil;
import com.itheima.bigevent.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private static final Logger log = LoggerFactory.getLogger(LoginInterceptor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response,
            @NonNull final Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // Allow public access to article and category lists/details if no token
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 允许公开访问的GET接口（即使没有Token或Token无效）
        if ("GET".equalsIgnoreCase(method)) {
            if (uri.startsWith("/article") || uri.startsWith("/category") ||
                    uri.startsWith("/comment") || uri.startsWith("/message") ||
                    uri.startsWith("/album") || // 允许匿名访问相册列表和详情
                    uri.startsWith("/statistics/view") || uri.startsWith("/statistics/hot")) {
                String token = request.getHeader(AUTHORIZATION_HEADER);
                if (token == null || token.isBlank()) {
                    log.debug("[Token验证] 允许匿名访问GET接口: {} {}", method, uri);
                    return true; // 允许匿名访问，不设置ThreadLocal
                }
                // 如果有token，尝试验证；如果验证失败，仍然允许匿名访问（仅对GET请求）
                try {
                    String cleanToken = token.trim().replaceAll("[\\r\\n\\t]", "");
                    if (cleanToken.isEmpty() || !cleanToken.contains(".") || cleanToken.split("\\.").length != 3) {
                        log.debug("[Token验证] Token格式无效，允许匿名访问GET接口: {} {}", method, uri);
                        return true; // Token格式无效，允许匿名访问
                    }
                    // 验证token是否在Redis中存在
                    final ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
                    String redisToken = operations.get(cleanToken);
                    if (redisToken == null) {
                        log.debug("[Token验证] Token不存在或已过期，允许匿名访问GET接口: {} {}", method, uri);
                        return true; // Token无效，允许匿名访问
                    }
                    // Token有效，解析并设置ThreadLocal
                    Map<String, Object> map = JwtUtil.parseToken(cleanToken);
                    if (map != null && !map.isEmpty() && map.get("id") != null) {
                        ThreadLocalUtil.set(map);
                        log.debug("[Token验证] Token有效，已设置用户上下文: {} {} - 用户ID: {}", method, uri, map.get("id"));
                    }
                    return true; // 允许访问
                } catch (Exception e) {
                    // Token验证失败，允许匿名访问（仅对GET请求）
                    log.debug("[Token验证] Token验证失败，允许匿名访问GET接口: {} {} - 错误: {}", method, uri, e.getMessage());
                    return true;
                }
            }
        }

        // 允许公开访问的POST接口（访问量记录）
        if ("POST".equalsIgnoreCase(method)) {
            if (uri.startsWith("/statistics/view")) {
                return true; // 允许匿名记录访问量，不设置ThreadLocal
            }
        }

        // 对于需要认证的请求，必须验证token
        final String token = request.getHeader(AUTHORIZATION_HEADER);
        try {
            if (token == null || token.isBlank()) {
                log.warn("[Token验证] 请求缺少Token: {} {} - 请求头列表: {}", method, uri,
                        java.util.Collections.list(request.getHeaderNames()));
                writeErrorResponse(response, "用户未登录或登录已过期");
                return false;
            }

            // 清理token中的空格、换行等字符
            String cleanToken = token.trim();
            // 移除可能的换行符和制表符
            cleanToken = cleanToken.replaceAll("[\\r\\n\\t]", "");

            // 验证token基本格式（JWT通常包含两个点）
            if (cleanToken.isEmpty()) {
                log.warn("[Token验证] Token为空字符串: {} {}", method, uri);
                writeErrorResponse(response, "Token格式错误，请重新登录");
                return false;
            }

            // 验证JWT格式（应该包含两个点分隔符）
            if (!cleanToken.contains(".") || cleanToken.split("\\.").length != 3) {
                log.warn("[Token验证] Token格式不正确（不是有效的JWT）: {} {} - Token长度: {} - Token前缀: {}",
                        method, uri, cleanToken.length(),
                        cleanToken.length() > 30 ? cleanToken.substring(0, 30) + "..." : cleanToken);
                writeErrorResponse(response, "Token格式错误，请重新登录");
                return false;
            }

            log.info("[Token验证] 收到Token验证请求: {} {} - Token长度: {} - Token前缀: {}",
                    method, uri, cleanToken.length(),
                    cleanToken.length() > 20 ? cleanToken.substring(0, 20) + "..." : cleanToken);

            // 从Redis获取token
            final ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String redisToken = null;
            try {
                log.debug("[Token验证] 开始查询Redis: {} {} - Token前缀: {}",
                        method, uri, cleanToken.length() > 20 ? cleanToken.substring(0, 20) + "..." : cleanToken);
                redisToken = operations.get(cleanToken);
                log.info("[Token验证] Redis查找结果: {} {} - 是否存在: {} - Token长度: {}",
                        method, uri, redisToken != null, cleanToken.length());

                // 如果token不存在，尝试检查Redis中是否有类似的key
                if (redisToken == null) {
                    log.warn("[Token验证] Token在Redis中不存在，尝试检查Redis连接和key数量");
                    try {
                        Long keyCount = stringRedisTemplate.getConnectionFactory()
                                .getConnection().dbSize();
                        log.warn("[Token验证] Redis中总key数量: {}", keyCount);
                    } catch (Exception e) {
                        log.warn("[Token验证] 无法获取Redis key数量: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("[Token验证] Redis查询异常: {} {} - 错误: {}", method, uri, e.getMessage(), e);
                // 检查Redis连接是否正常
                try {
                    stringRedisTemplate.hasKey("test");
                    log.debug("[Token验证] Redis连接正常");
                } catch (Exception redisEx) {
                    log.error("[Token验证] Redis连接异常", redisEx);
                }
            }

            if (redisToken == null) {
                log.error("[Token验证] Token在Redis中不存在或已过期: {} {} - Token长度: {} - Token前缀: {} - 完整Token前50字符: {}",
                        method, uri, cleanToken.length(),
                        cleanToken.length() > 20 ? cleanToken.substring(0, 20) + "..." : cleanToken,
                        cleanToken.length() > 50 ? cleanToken.substring(0, 50) + "..." : cleanToken);
                writeErrorResponse(response, "用户未登录或登录已过期");
                return false;
            }

            // 验证token格式和解析
            Map<String, Object> map = null;
            try {
                map = JwtUtil.parseToken(cleanToken);
            } catch (com.auth0.jwt.exceptions.JWTVerificationException e) {
                log.warn("[Token验证] Token解析失败（JWT验证异常）: {} {} - 错误: {}",
                        method, uri, e.getMessage());
                writeErrorResponse(response, "Token验证失败，请重新登录");
                return false;
            } catch (Exception e) {
                log.warn("[Token验证] Token解析失败（其他异常）: {} {} - 错误: {}",
                        method, uri, e.getMessage());
                writeErrorResponse(response, "Token解析失败，请重新登录");
                return false;
            }

            if (map == null || map.isEmpty()) {
                log.warn("[Token验证] Token解析结果为空: {} {}", method, uri);
                writeErrorResponse(response, "Token解析失败，请重新登录");
                return false;
            }

            // 验证用户ID是否存在
            Object userId = map.get("id");
            if (userId == null) {
                log.warn("[Token验证] Token中缺少用户ID: {} {} - Token内容: {}",
                        method, uri, map.keySet());
                writeErrorResponse(response, "Token信息不完整，请重新登录");
                return false;
            }

            log.debug("[Token验证] Token验证成功: {} {} - 用户ID: {}", method, uri, userId);

            // 设置ThreadLocal
            ThreadLocalUtil.set(map);
            return true;
        } catch (com.auth0.jwt.exceptions.JWTVerificationException e) {
            log.warn("[Token验证] Token验证失败（JWT异常）: {} {} - 错误: {}",
                    method, uri, e.getMessage());
            writeErrorResponse(response, "Token验证失败，请重新登录");
            return false;
        } catch (Exception e) {
            log.error("[Token验证] Token验证异常（未知异常）: {} {} - 错误: {}",
                    method, uri, e.getMessage(), e);
            writeErrorResponse(response, "用户未登录或登录已过期");
            return false;
        }
    }

    @Override
    public void afterCompletion(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response,
            @NonNull final Object handler, @Nullable final Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, String message) {
        try {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");

            Result<?> result = Result.error(ResultCode.UNAUTHORIZED, message);
            String json = objectMapper.writeValueAsString(result);

            PrintWriter writer = response.getWriter();
            writer.write(json);
            writer.flush();
        } catch (Exception e) {
            log.error("写入错误响应失败", e);
        }
    }
}
