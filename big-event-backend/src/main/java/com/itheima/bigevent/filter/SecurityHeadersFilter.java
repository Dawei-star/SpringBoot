package com.itheima.bigevent.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 安全响应头过滤器
 * 添加各种安全响应头，防止XSS、点击劫持等攻击
 */
@Component
@Order(1) // 确保在其他过滤器之前执行
public class SecurityHeadersFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    HttpServletRequest httpRequest = (HttpServletRequest) request;

    // 防止MIME类型嗅探
    httpResponse.setHeader("X-Content-Type-Options", "nosniff");

    // 防止点击劫持（X-Frame-Options）
    httpResponse.setHeader("X-Frame-Options", "DENY");

    // XSS保护（虽然现代浏览器已内置，但保留以兼容旧浏览器）
    httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

    // 内容安全策略（CSP）- 根据实际情况调整
    // 注意：如果使用富文本编辑器，可能需要调整script-src和style-src
    String csp = "default-src 'self'; " +
        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " + // 允许内联脚本（富文本编辑器需要）
        "style-src 'self' 'unsafe-inline'; " + // 允许内联样式
        "img-src 'self' data: https:; " + // 允许图片（包括base64和https）
        "font-src 'self' data:; " + // 允许字体
        "connect-src 'self'; " + // 允许AJAX请求
        "frame-ancestors 'none';"; // 防止被嵌入iframe
    httpResponse.setHeader("Content-Security-Policy", csp);

    // 推荐策略（CSP的简化版本，用于不支持CSP的旧浏览器）
    httpResponse.setHeader("X-Content-Security-Policy", csp);

    // HSTS（HTTP严格传输安全）- 仅在生产环境HTTPS下启用
    // 开发环境不要启用，否则会导致HTTP无法访问
    String scheme = httpRequest.getScheme();
    if ("https".equalsIgnoreCase(scheme)) {
      httpResponse.setHeader("Strict-Transport-Security",
          "max-age=31536000; includeSubDomains; preload");
    }

    // Referrer策略
    httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

    // 权限策略（Feature Policy的替代）
    httpResponse.setHeader("Permissions-Policy",
        "geolocation=(), microphone=(), camera=()");

    chain.doFilter(request, response);
  }
}
