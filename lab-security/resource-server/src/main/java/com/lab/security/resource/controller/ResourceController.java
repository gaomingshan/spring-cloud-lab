package com.lab.security.resource.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.lab.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 资源访问 Controller
 * 演示 Sa-Token 注解式权限校验
 */
@Slf4j
@RestController
@RequestMapping("/resource")
public class ResourceController {

    /**
     * 需要登录才能访问（@SaCheckLogin）
     */
    @SaCheckLogin
    @GetMapping("/user-info")
    public Result<Map<String, Object>> userInfo() {
        String loginId = StpUtil.getLoginIdAsString();
        log.info("[Resource] 获取用户信息: loginId={}", loginId);
        return Result.ok(Map.of(
                "loginId", loginId,
                "tokenValue", StpUtil.getTokenValue(),
                "loginDevice", StpUtil.getLoginDevice()
        ));
    }

    /**
     * 需要特定权限（@SaCheckPermission）
     * 权限码由 StpInterface.getPermissionList() 返回
     */
    @SaCheckPermission("user:delete")
    @DeleteMapping("/user/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        log.info("[Resource] 删除用户: id={}", id);
        return Result.ok("删除成功");
    }

    /**
     * 需要特定角色（@SaCheckRole）
     */
    @SaCheckRole("admin")
    @GetMapping("/admin")
    public Result<String> adminOnly() {
        return Result.ok("管理员专属接口");
    }

    /**
     * 公开接口（无需鉴权）
     */
    @GetMapping("/public")
    public Result<String> publicApi() {
        return Result.ok("公开接口，无需登录");
    }
}
