package com.lab.security.auth.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.lab.common.result.Result;
import com.lab.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证 Controller
 * 演示 Sa-Token 登录、注销、Token 查询全流程
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    // 模拟用户数据库（实际应查 MySQL）
    // 密码使用 BCrypt 加密存储：BCrypt.hashpw("123456", BCrypt.gensalt())
    private static final Map<String, String> USER_DB = Map.of(
            "admin", BCrypt.hashpw("123456", BCrypt.gensalt()),
            "user1", BCrypt.hashpw("password", BCrypt.gensalt())
    );

    /**
     * 登录接口
     * Sa-Token 登录后自动生成 Token 并写入 Redis
     */
    @PostMapping("/login")
    public Result<SaTokenInfo> login(
            @RequestParam String username,
            @RequestParam String password) {
        String hashedPwd = USER_DB.get(username);
        if (hashedPwd == null || !BCrypt.checkpw(password, hashedPwd)) {
            log.warn("[Auth] 登录失败: username={}", username);
            return Result.fail(ResultCode.UNAUTHORIZED);
        }
        // 登录，deviceType 区分设备（PC/APP/小程序）
        StpUtil.login(username, "PC");
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        log.info("[Auth] 登录成功: username={}, token={}", username, tokenInfo.getTokenValue());
        return Result.ok(tokenInfo);
    }

    /**
     * 注销接口
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        String loginId = StpUtil.getLoginIdAsString();
        StpUtil.logout();
        log.info("[Auth] 注销: loginId={}", loginId);
        return Result.ok();
    }

    /**
     * 获取当前 Token 信息
     */
    @GetMapping("/token-info")
    public Result<SaTokenInfo> tokenInfo() {
        StpUtil.checkLogin();
        return Result.ok(StpUtil.getTokenInfo());
    }

    /**
     * 踢人下线（管理员操作）
     */
    @PostMapping("/kick-out")
    public Result<Void> kickOut(@RequestParam String userId) {
        StpUtil.kickout(userId);
        log.info("[Auth] 踢人下线: userId={}", userId);
        return Result.ok();
    }
}
