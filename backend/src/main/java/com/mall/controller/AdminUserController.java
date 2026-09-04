package com.mall.controller;

import com.mall.common.annotation.RequireLogin;
import com.mall.common.annotation.RequirePermission;
import com.mall.common.result.Result;
import com.mall.context.AdminContext;
import com.mall.service.AdminUserService;
import com.mall.vo.AdminUserVO;
import com.mall.vo.MenuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台用户Controller
 *
 * @author mall
 */
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
@Tag(name = "后台用户", description = "后台用户信息接口")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    @RequireLogin
    @Operation(summary = "用户信息", description = "获取当前登录用户信息")
    public Result<AdminUserVO> getUserInfo() {
        Long userId = AdminContext.getAdminId();
        AdminUserVO userInfo = adminUserService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    /**
     * 获取用户菜单
     */
    @GetMapping("/menus")
    @RequireLogin
    @Operation(summary = "用户菜单", description = "获取当前用户的菜单权限")
    public Result<List<MenuVO>> getUserMenus() {
        Long userId = AdminContext.getAdminId();
        List<MenuVO> menus = adminUserService.getUserMenus(userId);
        return Result.success(menus);
    }

    /**
     * 获取用户权限
     */
    @GetMapping("/permissions")
    @RequireLogin
    @Operation(summary = "用户权限", description = "获取当前用户的按钮权限")
    public Result<List<String>> getUserPermissions() {
        Long userId = AdminContext.getAdminId();
        List<String> permissions = adminUserService.getUserPermissions(userId);
        return Result.success(permissions);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    @RequireLogin
    @Operation(summary = "修改密码", description = "修改当前用户密码")
    public Result<Boolean> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        Long userId = AdminContext.getAdminId();
        Boolean result = adminUserService.changePassword(userId, oldPassword, newPassword);
        return Result.success("密码修改成功", result);
    }
}
