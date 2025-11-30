package com.itheima.bigevent.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret:}")
    private String secretKey;

    private static String KEY;

    // 普通token有效期：2小时
    private static final long NORMAL_TOKEN_EXPIRE = 1000L * 60 * 60 * 2;

    // 记住我token有效期：7天
    private static final long REMEMBER_ME_TOKEN_EXPIRE = 1000L * 60 * 60 * 24 * 7;

    /**
     * 初始化JWT密钥
     * 从配置文件读取，如果未配置则从环境变量读取
     */
    @PostConstruct
    public void init() {
        // 优先使用配置文件中的密钥
        if (secretKey != null && !secretKey.trim().isEmpty()) {
            KEY = secretKey.trim();
        } else {
            // 从环境变量读取
            KEY = System.getenv("JWT_SECRET");
        }

        // 如果仍然为空，使用默认值（仅用于开发环境，生产环境必须配置）
        if (KEY == null || KEY.trim().isEmpty()) {
            String defaultKey = System.getProperty("jwt.secret");
            if (defaultKey != null && !defaultKey.trim().isEmpty()) {
                KEY = defaultKey.trim();
            } else {
                // 开发环境警告，生产环境应该抛出异常
                System.err.println("⚠️ 警告: JWT密钥未配置！请设置 jwt.secret 或 JWT_SECRET 环境变量");
                System.err.println("⚠️ 当前使用默认密钥，这是不安全的！生产环境必须配置强密钥！");
                KEY = "itheima"; // 临时默认值，仅用于开发
            }
        }

        // 验证密钥强度
        if (KEY.length() < 32) {
            System.err.println("⚠️ 警告: JWT密钥长度不足32位，建议使用至少32位的强随机密钥！");
        }
    }

    // 接收业务数据, 生成 token 并返回（普通token，2小时）
    public static String genToken(Map<String, Object> claims) {
        return genToken(claims, false);
    }

    // 接收业务数据, 生成 token 并返回
    // isRememberMe: true表示"记住我"，有效期7天；false表示普通登录，有效期2小时
    public static String genToken(Map<String, Object> claims, boolean isRememberMe) {
        long expireTime = isRememberMe ? REMEMBER_ME_TOKEN_EXPIRE : NORMAL_TOKEN_EXPIRE;
        return JWT.create()
                .withClaim("claims", claims)
                .withClaim("rememberMe", isRememberMe)
                .withExpiresAt(new Date(System.currentTimeMillis() + expireTime))
                .sign(Algorithm.HMAC256(KEY));
    }

    // 刷新token（生成新的token，保持原有的rememberMe状态）
    public static String refreshToken(String oldToken) {
        try {
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(KEY))
                    .build()
                    .verify(oldToken);

            Map<String, Object> claims = decodedJWT.getClaim("claims").asMap();
            Boolean rememberMe = decodedJWT.getClaim("rememberMe").asBoolean();

            if (rememberMe == null) {
                rememberMe = false;
            }

            // 生成新token，保持原有的rememberMe状态
            return genToken(claims, rememberMe);
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Token刷新失败: " + e.getMessage());
        }
    }

    // 接收token,验证token,并返回业务数据
    public static Map<String, Object> parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(KEY))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }

    // 检查token是否即将过期（剩余时间少于30分钟）
    public static boolean isTokenExpiringSoon(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(KEY))
                    .build()
                    .verify(token);

            Date expiresAt = decodedJWT.getExpiresAt();
            long remainingTime = expiresAt.getTime() - System.currentTimeMillis();

            // 剩余时间少于30分钟
            return remainingTime < 1000L * 60 * 30;
        } catch (JWTVerificationException e) {
            return true; // token无效，视为即将过期
        }
    }
}
