package com.mall.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.config.GithubProperties;
import com.mall.entity.UserDO;
import com.mall.mapper.UserMapper;
import com.mall.service.GithubLoginService;
import com.mall.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * GitHub 登录服务实现类
 *
 * @author system
 * @date 2026/09/15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GithubLoginServiceImpl implements GithubLoginService {

    private final GithubProperties githubProperties;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    /**
     * GitHub 授权 URL 缓存 Key
     */
    private static final String STATE_CACHE_KEY = "github:oauth:state:";

    /**
     * State 过期时间（分钟）
     */
    private static final long STATE_EXPIRE_MINUTES = 10;

    /**
     * 获取 GitHub 授权 URL
     */
    @Override
    public String getAuthorizeUrl(String state) {
        // 将 state 存入 Redis，用于回调时验证
        String stateKey = STATE_CACHE_KEY + state;
        redisTemplate.opsForValue().set(stateKey, "1", STATE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 构建授权 URL
        return githubProperties.getAuthorizeUrl() + "?" +
                "client_id=" + githubProperties.getClientId() +
                "&redirect_uri=" + githubProperties.getRedirectUri() +
                "&scope=" + githubProperties.getScope() +
                "&state=" + state;
    }

    /**
     * GitHub 授权回调处理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public GithubLoginVO callback(String code, String state) {
        // 1. 验证 state（防止 CSRF 攻击）
        validateState(state);

        // 2. 通过 code 获取 access_token
        String accessToken = getAccessToken(code);

        // 3. 通过 access_token 获取用户信息
        Map<String, Object> githubUserInfo = getGithubUserInfo(accessToken);

        if (githubUserInfo == null) {
            throw new BusinessException("G0001", "获取GitHub用户信息失败");
        }

        // 4. 提取用户信息
        String githubId = String.valueOf(githubUserInfo.get("id"));
        String githubUsername = (String) githubUserInfo.get("login");
        String avatar = (String) githubUserInfo.get("avatar_url");
        String email = (String) githubUserInfo.get("email");

        // 如果 GitHub 没有返回邮箱，尝试获取
        if (StrUtil.isBlank(email)) {
            email = getGithubEmail(accessToken);
        }

        // 5. 获取或创建用户
        return getOrCreateUser(githubId, githubUsername, avatar, email);
    }

    /**
     * 验证 state
     */
    private void validateState(String state) {
        if (StrUtil.isBlank(state)) {
            throw new BusinessException("G0002", "state参数不能为空");
        }

        String stateKey = STATE_CACHE_KEY + state;
        Boolean exists = redisTemplate.hasKey(stateKey);

        if (exists == null || !exists) {
            throw new BusinessException("G0003", "state验证失败，请重新授权");
        }

        // 删除 state，防止重复使用
        redisTemplate.delete(stateKey);
    }

    /**
     * 获取 access_token
     */
    private String getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        // 构建请求参数
        Map<String, String> params = new HashMap<>();
        params.put("client_id", githubProperties.getClientId());
        params.put("client_secret", githubProperties.getClientSecret());
        params.put("code", code);
        params.put("redirect_uri", githubProperties.getRedirectUri());

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    githubProperties.getTokenUrl(),
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String accessToken = (String) body.get("access_token");

                if (StrUtil.isBlank(accessToken)) {
                    String error = (String) body.get("error");
                    throw new BusinessException("G0004", "获取access_token失败: " + error);
                }

                return accessToken;
            }
        } catch (RestClientException e) {
            log.error("获取GitHub access_token失败", e);
            throw new BusinessException("G0004", "获取access_token失败");
        }

        throw new BusinessException("G0004", "获取access_token失败");
    }

    /**
     * 获取 GitHub 用户信息
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getGithubUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    githubProperties.getUserInfoUrl(),
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (RestClientException e) {
            log.error("获取GitHub用户信息失败", e);
        }

        return null;
    }

    /**
     * 获取 GitHub 用户邮箱
     */
    @SuppressWarnings("unchecked")
    private String getGithubEmail(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(
                    githubProperties.getUserEmailsUrl(),
                    HttpMethod.GET,
                    request,
                    List.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> emails = response.getBody();

                // 优先查找主邮箱
                for (Map<String, Object> emailInfo : emails) {
                    Boolean primary = (Boolean) emailInfo.get("primary");
                    Boolean verified = (Boolean) emailInfo.get("verified");

                    if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                        return (String) emailInfo.get("email");
                    }
                }

                // 没有主邮箱，返回第一个已验证的邮箱
                for (Map<String, Object> emailInfo : emails) {
                    Boolean verified = (Boolean) emailInfo.get("verified");
                    if (Boolean.TRUE.equals(verified)) {
                        return (String) emailInfo.get("email");
                    }
                }
            }
        } catch (RestClientException e) {
            log.warn("获取GitHub邮箱失败", e);
        }

        return null;
    }

    /**
     * 获取或创建用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public GithubLoginVO getOrCreateUser(String githubId, String githubUsername, String avatar, String email) {
        // 1. 查找是否已存在该 GitHub 用户
        UserDO user = userMapper.selectOne(
                new LambdaQueryWrapper<UserDO>()
                        .eq(UserDO::getGithubId, githubId)
                        .eq(UserDO::getLoginType, 1) // 1 = GitHub登录
                        .last("LIMIT 1")
        );

        boolean isNewUser = false;

        // 2. 如果不存在，创建新用户
        if (Objects.isNull(user)) {
            isNewUser = true;

            user = new UserDO();
            user.setGithubId(githubId);
            user.setGithubUsername(githubUsername);
            user.setAvatar(avatar);
            user.setEmail(email);
            user.setLoginType(1); // GitHub登录

            // 生成用户名：github_xxxxxx
            String username = "github_" + StrUtil.sub(githubId, 0, Math.min(8, githubId.length()));
            
            // 检查用户名是否已存在
            UserDO existingUser = userMapper.selectOne(
                    new LambdaQueryWrapper<UserDO>()
                            .eq(UserDO::getUsername, username)
                            .last("LIMIT 1")
            );
            
            if (Objects.nonNull(existingUser)) {
                username = username + "_" + IdUtil.fastSimpleUUID().substring(0, 4);
            }
            user.setUsername(username);
            
            // 生成随机昵称
            user.setNickname("GitHub用户_" + IdUtil.fastSimpleUUID().substring(0, 6));
            
            // 默认启用
            user.setStatus(1);

            userMapper.insert(user);

            log.info("创建GitHub新用户, githubId={}, username={}", githubId, username);
        } else {
            // 3. 如果存在，更新用户信息
            boolean needUpdate = false;
            
            if (StrUtil.isNotBlank(avatar) && !avatar.equals(user.getAvatar())) {
                user.setAvatar(avatar);
                needUpdate = true;
            }
            
            if (StrUtil.isNotBlank(githubUsername) && !githubUsername.equals(user.getGithubUsername())) {
                user.setGithubUsername(githubUsername);
                needUpdate = true;
            }
            
            if (needUpdate) {
                userMapper.updateById(user);
            }
        }

        // 4. 生成 JWT Token
        String token = jwtUtil.generateGithubToken(user.getId(), user.getUsername(), user.getLoginType());

        // 5. 构建返回结果
        return GithubLoginVO.builder()
                .isNewUser(isNewUser)
                .token(token)
                .tokenType("Bearer")
                .githubId(githubId)
                .githubUsername(githubUsername)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .build();
    }
}
