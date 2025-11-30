package com.itheima.bigevent.controller;

import com.itheima.bigevent.anno.OpLog;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.pojo.ResultCode;
import com.itheima.bigevent.pojo.User;
import com.itheima.bigevent.service.UserService;
import com.itheima.bigevent.utils.JwtUtil;
import com.itheima.bigevent.utils.ThreadLocalUtil;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Objects;
import java.util.Map;

import static com.itheima.bigevent.interceptor.LoginInterceptor.AUTHORIZATION_HEADER;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @OpLog(module = "用户管理", operation = "用户注册")
    @PostMapping("/register")
    public Result<User> register(@Pattern(regexp = "^\\S{5,16}$") final String username,
            @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$") final String password) {
        final User existingUser = userService.findByUsername(username);
        if (existingUser == null) {
            userService.register(username, password);
            return Result.success(ResultCode.REGISTER_SUCCESS, "注册成功");
        } else {
            return Result.error(ResultCode.USERNAME_TAKEN);
        }
    }

    @OpLog(module = "用户管理", operation = "用户登录")
    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp = "^\\S{5,16}$") final String username,
            final String password, // 登录时不需要严格验证密码格式（因为可能是旧密码）
            @RequestParam(required = false, defaultValue = "false") final Boolean rememberMe) {
        final User existingUser = userService.findByUsername(username);
        if (existingUser == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        if (passwordEncoder.matches(password, existingUser.getPassword())) {
            final Map<String, Object> map = Map.of("id", existingUser.getId(),
                    "username", existingUser.getUsername());
            // 根据rememberMe参数生成不同有效期的token
            final String token = JwtUtil.genToken(map, rememberMe != null && rememberMe);
            // Save token to redis
            final ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            // 根据rememberMe设置不同的过期时间
            final Duration ttl = (rememberMe != null && rememberMe)
                    ? Duration.ofDays(7) // 记住我：7天
                    : Duration.ofHours(2); // 普通登录：2小时

            // 记录token存储信息（用于调试）
            log.debug("[登录] Token已生成: 长度={}, 前缀={}, rememberMe={}, TTL={}小时",
                    token.length(),
                    token.length() > 20 ? token.substring(0, 20) + "..." : token,
                    rememberMe != null && rememberMe,
                    ttl.toHours());

            try {
                operations.set(Objects.requireNonNull(token, "token"), Objects.requireNonNull(token, "token"),
                        Objects.requireNonNull(ttl, "ttl"));
                log.debug("[登录] Token已存储到Redis: 长度={}, 验证存储={}",
                        token.length(),
                        operations.get(token) != null ? "成功" : "失败");
            } catch (Exception e) {
                log.error("[登录] Token存储到Redis失败: {}", e.getMessage(), e);
                throw new RuntimeException("Token存储失败", e);
            }

            // Return JWT token
            // 使用明确的方法调用，避免方法重载冲突
            return Result.success(ResultCode.LOGIN_SUCCESS, ResultCode.LOGIN_SUCCESS.getMessage(), token);
        } else {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }
    }

    @OpLog(module = "用户管理", operation = "刷新Token")
    @PostMapping("/refresh")
    public Result<String> refreshToken(@RequestHeader(AUTHORIZATION_HEADER) final String token) {
        try {
            // 验证旧token
            final ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            final String redisToken = operations.get(Objects.requireNonNull(token, "Token不能为空"));
            if (redisToken == null) {
                return Result.error(ResultCode.TOKEN_EXPIRED);
            }

            // 刷新token
            final String newToken = JwtUtil.refreshToken(Objects.requireNonNull(token, "Token不能为空"));

            // 删除旧token
            operations.getOperations().delete(Objects.requireNonNull(token, "Token不能为空"));

            // 保存新token到redis
            // 检查新token的rememberMe状态
            try {
                com.auth0.jwt.interfaces.DecodedJWT decodedJWT = com.auth0.jwt.JWT.decode(newToken);
                Boolean rememberMe = decodedJWT.getClaim("rememberMe").asBoolean();
                final Duration ttl = (rememberMe != null && rememberMe)
                        ? Duration.ofDays(7)
                        : Duration.ofHours(2);
                operations.set(
                        Objects.requireNonNull(newToken, "新Token不能为空"),
                        Objects.requireNonNull(newToken, "新Token不能为空"),
                        Objects.requireNonNull(ttl, "TTL不能为空"));
            } catch (Exception e) {
                // 如果无法解析，使用默认2小时
                operations.set(
                        Objects.requireNonNull(newToken, "新Token不能为空"),
                        Objects.requireNonNull(newToken, "新Token不能为空"),
                        Objects.requireNonNull(Duration.ofHours(2), "TTL不能为空"));
            }

            // 使用明确的方法调用，避免方法重载冲突
            return Result.success(ResultCode.SUCCESS, ResultCode.SUCCESS.getMessage(), newToken);
        } catch (Exception e) {
            return Result.error(ResultCode.TOKEN_INVALID, "Token刷新失败: " + e.getMessage());
        }
    }

    @GetMapping("/userInfo")
    public Result<User> userInfo() {
        final Map<String, Object> map = ThreadLocalUtil.get();
        final String username = Objects.requireNonNull((String) map.get("username"), "用户名不能为空");
        final User user = userService.findByUsername(username);
        return Result.success(user);
    }

    @OpLog(module = "用户管理", operation = "修改用户资料")
    @PutMapping("/update")
    public Result<String> update(@RequestBody @Validated final User user) {
        final Map<String, Object> map = ThreadLocalUtil.get();
        final Integer id = (Integer) map.get("id");
        if (user.getId().equals(id)) {
            userService.update(user);
            return Result.success(ResultCode.UPDATED, "用户信息更新成功");
        } else {
            return Result.error(ResultCode.FORBIDDEN, "只能修改自己的信息");
        }
    }

    @OpLog(module = "用户管理", operation = "修改头像")
    @PatchMapping("/updateAvatar")
    public Result<String> updateAvatar(@RequestParam @URL final String avatarUrl) {
        userService.updateAvatar(avatarUrl);
        return Result.success(ResultCode.UPDATED, "头像更新成功");
    }

    @OpLog(module = "用户管理", operation = "修改密码")
    @PatchMapping("/updatePwd")
    public Result<String> updatePwd(@RequestBody Map<String, String> params,
            @RequestHeader(AUTHORIZATION_HEADER) final String token) {
        final String oldPwd = params.get("old_pwd");
        final String newPwd = params.get("new_pwd");
        final String rePwd = params.get("re_pwd");
        if (!StringUtils.hasLength(oldPwd) || !StringUtils.hasLength(newPwd) || !StringUtils.hasLength(rePwd)) {
            return Result.error(ResultCode.INVALID_PARAMETER, "缺少必要的参数");
        }

        final Map<String, Object> map = ThreadLocalUtil.get();
        final String username = (String) map.get("username");
        final User user = userService.findByUsername(username);
        if (!rePwd.equals(newPwd)) {
            return Result.error(ResultCode.PASSWORD_MISMATCH);
        }
        if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR, "原密码错误");
        }

        userService.updatePwd(newPwd);
        // Delete token in redis.
        final ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.getOperations().delete(Objects.requireNonNull(token, "token"));

        return Result.success(ResultCode.UPDATED, "密码修改成功，请重新登录");
    }

    @OpLog(module = "用户管理", operation = "获取用户列表")
    @GetMapping("/list")
    public Result<java.util.List<User>> list() {
        final Map<String, Object> map = ThreadLocalUtil.get();
        final String username = (String) map.get("username");
        final User currentUser = userService.findByUsername(username);

        if (currentUser == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }

        // 判断是否为管理员
        final boolean isAdmin = "admin".equals(currentUser.getRole());
        final java.util.List<User> users = userService.findAllUsers(isAdmin);

        // 移除密码信息
        users.forEach(u -> u.setPassword(null));

        return Result.success(users);
    }

    @OpLog(module = "用户管理", operation = "更新用户角色")
    @PatchMapping("/updateRole")
    public Result<String> updateRole(@RequestParam final Integer id, @RequestParam final String role) {
        final Map<String, Object> map = ThreadLocalUtil.get();
        final String username = (String) map.get("username");
        final User currentUser = userService.findByUsername(username);

        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            return Result.error(ResultCode.PERMISSION_DENIED);
        }

        if (!"admin".equals(role) && !"user".equals(role)) {
            return Result.error(ResultCode.INVALID_PARAMETER, "无效的角色");
        }

        userService.updateRole(id, role);
        return Result.success(ResultCode.UPDATED, "用户角色更新成功");
    }

    @OpLog(module = "用户管理", operation = "管理员更新用户信息")
    @PutMapping("/updateByAdmin")
    public Result<String> updateByAdmin(@RequestBody @Validated final User user) {
        final Map<String, Object> map = ThreadLocalUtil.get();
        final String username = (String) map.get("username");
        final User currentUser = userService.findByUsername(username);

        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            return Result.error(ResultCode.PERMISSION_DENIED);
        }

        userService.updateByAdmin(user);
        return Result.success(ResultCode.UPDATED, "用户信息更新成功");
    }
}
