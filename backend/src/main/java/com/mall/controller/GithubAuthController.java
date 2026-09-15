package com.mall.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.mall.common.result.Result;
import com.mall.service.GithubLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * GitHub 登录 Controller
 *
 * @author system
 * @date 2026/09/15
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/github")
@RequiredArgsConstructor
@Tag(name = "GitHub登录", description = "GitHub OAuth第三方登录接口")
public class GithubAuthController {

    private final GithubLoginService githubLoginService;

    /**
     * 获取 GitHub 授权 URL
     * <p>
     * 前端调用此接口获取授权跳转地址，然后跳转到该地址
     *
     * @return 授权跳转 URL
     */
    @GetMapping("/authorize")
    @Operation(summary = "获取GitHub授权URL", description = "获取GitHub授权跳转URL，前端跳转到此URL进行授权")
    public Result<Map<String, String>> getAuthorizeUrl() {
        // 生成随机 state，用于防止 CSRF 攻击
        String state = IdUtil.fastSimpleUUID();

        // 获取授权 URL
        String authorizeUrl = githubLoginService.getAuthorizeUrl(state);

        Map<String, String> result = new HashMap<>(1);
        result.put("authorizeUrl", authorizeUrl);
        result.put("state", state);

        log.info("生成GitHub授权URL, state={}", state);

        return Result.success(result);
    }

    /**
     * GitHub 授权回调
     * <p>
     * GitHub 授权后会回调此接口，带上 code 和 state 参数
     * 注意：由于 GitHub OAuth 回调是 GET 请求，且受限于 GitHub 的回调配置，
     * 实际生产环境中，回调 URL 需要在 GitHub OAuth App 设置中配置
     *
     * @param code  授权码
     * @param state 状态码
     * @return 登录结果
     */
    @GetMapping("/callback")
    @Operation(summary = "GitHub授权回调", description = "GitHub授权后的回调接口，处理授权码并完成登录")
    public Result<Map<String, Object>> callback(
            @Parameter(description = "授权码")
            @RequestParam String code,

            @Parameter(description = "状态码")
            @RequestParam String state) {

        log.info("GitHub授权回调, code={}, state={}", code, state);

        // 处理回调，返回登录结果
        var loginResult = githubLoginService.callback(code, state);

        // 构建结果
        Map<String, Object> result = new HashMap<>();
        result.put("isNewUser", loginResult.getIsNewUser());
        result.put("token", loginResult.getToken());
        result.put("tokenType", loginResult.getTokenType());
        result.put("userId", loginResult.getUserId());
        result.put("username", loginResult.getUsername());
        result.put("nickname", loginResult.getNickname());
        result.put("avatar", loginResult.getAvatar());
        result.put("email", loginResult.getEmail());
        result.put("githubUsername", loginResult.getGithubUsername());

        return Result.success(result);
    }

    /**
     * 重定向到 GitHub 授权页面
     * <p>
     * 如果前端需要后端协助跳转，可以调用此接口
     *
     * @param response HTTP 响应
     */
    @GetMapping("/redirect")
    @Operation(summary = "重定向到GitHub授权", description = "后端协助重定向到GitHub授权页面")
    public void redirectToGithub(HttpServletResponse response) {
        String state = IdUtil.fastSimpleUUID();
        String authorizeUrl = githubLoginService.getAuthorizeUrl(state);

        log.info("重定向到GitHub授权, state={}", state);

        try {
            response.sendRedirect(authorizeUrl);
        } catch (IOException e) {
            log.error("重定向失败", e);
        }
    }
}
