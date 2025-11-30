package com.itheima.bigevent.config;

import com.itheima.bigevent.interceptor.LoginInterceptor;
import com.itheima.bigevent.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Objects;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final List<String> BYPASS_PATHS = List.of(
            "/user/login",
            "/user/register",
            // 注意：不排除 /category 和 /article，让拦截器根据请求方法决定是否需要认证
            // GET 请求允许匿名访问，POST/PUT/DELETE 需要认证
            // 允许匿名访问统计接口（访问量统计）
            "/statistics/view",
            "/statistics/hot");

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(@NonNull final InterceptorRegistry registry) {
        // 先注册速率限制拦截器（在登录拦截器之前）
        registry.addInterceptor(Objects.requireNonNull(rateLimitInterceptor))
                .addPathPatterns("/user/login", "/user/register");

        // 再注册登录拦截器
        registry.addInterceptor(Objects.requireNonNull(loginInterceptor))
                .excludePathPatterns(Objects.requireNonNull(BYPASS_PATHS));
    }

    /**
     * CORS 配置
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("http://localhost:*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
