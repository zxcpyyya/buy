package com.mall.controller;

import com.mall.common.annotation.RequireLogin;
import com.mall.common.result.Result;
import com.mall.context.AdminContext;
import com.mall.dto.AdminLoginDTO;
import com.mall.service.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 后台认证Controller
 *
 * @author mall
 */
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Tag(name = "后台认证", description = "后台管理员登录认证接口")
public class AdminAuthController {

    private final AdminAuthService authService;

    /**
     * 后台管理员登录
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "后台管理员登录接口")
    public Result<Map<String, Object>> login(@Valid @RequestBody AdminLoginDTO loginDTO) {
        Map<String, Object> result = authService.login(loginDTO);
        return Result.success("登录成功", result);
    }

    /**
     * 验证Token
     */
    @GetMapping("/verify")
    @RequireLogin
    @Operation(summary = "验证Token", description = "验证当前Token是否有效")
    public Result<Map<String, Object>> verify() {
        AdminContext.AdminUser admin = AdminContext.getAdmin();
        Map<String, Object> result = new HashMap<>();
        result.put("userId", admin.getId());
        result.put("username", admin.getUsername());
        result.put("userType", admin.getUserType());
        return Result.success(result);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    @RequireLogin
    @Operation(summary = "刷新Token", description = "刷新当前Token")
    public Result<String> refresh(@RequestHeader("Authorization") String token) {
        String newToken = authService.refreshToken(token.replace("Bearer ", ""));
        return Result.success("刷新成功", newToken);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @RequireLogin
    @Operation(summary = "退出登录", description = "退出当前登录状态")
    public Result<Boolean> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token.replace("Bearer ", ""));
        return Result.success("退出成功", true);
    }
}
