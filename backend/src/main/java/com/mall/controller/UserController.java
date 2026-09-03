package com.mall.controller;

import com.mall.common.result.Result;
import com.mall.context.UserContext;
import com.mall.dto.LoginDTO;
import com.mall.dto.RegisterDTO;
import com.mall.dto.UpdateUserDTO;
import com.mall.service.UserService;
import com.mall.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户Controller
 * 
 * 遵循RESTful API设计规范和阿里Java开发规约
 * 
 * @author mall
 * @date 2024/01/01
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、登录、信息管理接口")
public class UserController {
    
    /**
     * 用户服务
     */
    private final UserService userService;
    
    /**
     * 用户注册
     * 
     * POST /api/user/register
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户")
    public Result<Long> register(@Valid @RequestBody RegisterDTO registerDTO) {
        log.info("用户注册请求, username={}", registerDTO.getUsername());
        
        Long userId = userService.register(registerDTO);
        
        return Result.success("注册成功", userId);
    }
    
    /**
     * 用户登录
     * 
     * POST /api/user/login
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        log.info("用户登录请求, username={}", loginDTO.getUsername());
        
        String token = userService.login(loginDTO);
        
        // 返回Token和用户信息
        Map<String, Object> data = new HashMap<>(2);
        data.put("token", token);
        data.put("tokenType", "Bearer");
        
        return Result.success("登录成功", data);
    }
    
    /**
     * 获取当前用户信息
     * 
     * GET /api/user/info
     */
    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户信息")
    public Result<UserVO> getUserInfo() {
        Long userId = UserContext.getUserId();
        
        log.debug("获取用户信息请求, userId={}", userId);
        
        UserVO userVO = userService.getCurrentUser(userId);
        
        return Result.success(userVO);
    }
    
    /**
     * 更新用户信息
     * 
     * PUT /api/user/info
     */
    @PutMapping("/info")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户信息")
    public Result<Boolean> updateUserInfo(@RequestBody UpdateUserDTO updateDTO) {
        Long userId = UserContext.getUserId();
        
        log.info("更新用户信息请求, userId={}", userId);
        
        Boolean result = userService.updateUserInfo(userId, updateDTO);
        
        return Result.success(result);
    }
    
    /**
     * 修改密码
     * 
     * PUT /api/user/password
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "修改当前登录用户密码")
    public Result<Boolean> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        Long userId = UserContext.getUserId();
        
        log.info("修改密码请求, userId={}", userId);
        
        Boolean result = userService.changePassword(userId, oldPassword, newPassword);
        
        return Result.success(result);
    }
    
    /**
     * 用户登出
     * 
     * POST /api/user/logout
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出当前登录")
    public Result<Boolean> logout() {
        Long userId = UserContext.getUserId();
        
        log.info("用户登出请求, userId={}", userId);
        
        // JWT无状态，不需要服务端处理，客户端删除Token即可
        
        return Result.success(true);
    }
}
