package com.mall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.enums.UserType;
import com.mall.common.exception.BusinessException;
import com.mall.dto.AdminLoginDTO;
import com.mall.entity.SysAdminDO;
import com.mall.entity.SysPermissionDO;
import com.mall.entity.SysRoleDO;
import com.mall.mapper.SysAdminMapper;
import com.mall.mapper.SysPermissionMapper;
import com.mall.mapper.SysRoleMapper;
import com.mall.service.AdminAuthService;
import com.mall.service.JwtService;
import com.mall.vo.AdminUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 后台认证服务实现类
 *
 * @author xiu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final SysAdminMapper adminMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final JwtService jwtService;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    /**
     * 后台管理员登录
     */
    @Override
    public Map<String, Object> login(AdminLoginDTO loginDTO) {
        // 1. 验证用户名密码
        SysAdminDO admin = adminMapper.selectOne(
                new LambdaQueryWrapper<SysAdminDO>()
                        .eq(SysAdminDO::getUsername, loginDTO.getUsername())
                        .eq(SysAdminDO::getStatus, 1)
        );

        if (admin == null) {
            throw BusinessException.of("A0401", "用户名或密码错误");
        }

        // 2. 验证密码（使用BCrypt）
        if (!org.springframework.security.crypto.bcrypt.BCryptPasswordEncoderFactories.createEncoder()
                .matches(loginDTO.getPassword(), admin.getPassword())) {
            throw BusinessException.of("A0401", "用户名或密码错误");
        }

        // 3. 获取用户角色和权限
        List<SysRoleDO> roles = getUserRoles(admin.getId());
        List<SysPermissionDO> permissions = getUserPermissions(admin.getId());

        // 4. 构建用户信息
        AdminUserVO userVO = buildUserVO(admin, roles, permissions);

        // 5. 生成token
        String token = jwtService.generateToken(userVO);

        // 6. 更新登录信息
        admin.setLastLoginTime(LocalDateTime.now());
        adminMapper.updateById(admin);

        // 7. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", userVO);
        result.put("permissions", permissions.stream()
                .filter(p -> p.getPermissionType() == 2) // 按钮权限
                .map(SysPermissionDO::getCode)
                .collect(Collectors.toList()));

        log.info("后台管理员登录成功, username={}", loginDTO.getUsername());

        return result;
    }

    /**
     * 验证token
     */
    @Override
    public AdminUserVO verifyToken(String token) {
        if (StrUtil.isBlank(token)) {
            throw BusinessException.of(401, "请先登录");
        }

        // 验证token
        if (!jwtService.validateToken(token)) {
            throw BusinessException.of(401, "登录已过期，请重新登录");
        }

        // 解析token获取用户ID
        Long userId = jwtService.getUserIdFromToken(token);
        UserType userType = jwtService.getUserTypeFromToken(token);

        if (userType != UserType.ADMIN && userType != UserType.MERCHANT) {
            throw BusinessException.of(401, "无效的用户类型");
        }

        // 从数据库获取最新用户信息
        SysAdminDO admin = adminMapper.selectById(userId);
        if (admin == null || admin.getStatus() != 1) {
            throw BusinessException.of(401, "用户不存在或已被禁用");
        }

        // 获取角色和权限
        List<SysRoleDO> roles = getUserRoles(userId);
        List<SysPermissionDO> permissions = getUserPermissions(userId);

        return buildUserVO(admin, roles, permissions);
    }

    /**
     * 刷新token
     */
    @Override
    public String refreshToken(String token) {
        AdminUserVO userVO = verifyToken(token);
        return jwtService.generateToken(userVO);
    }

    /**
     * 退出登录
     */
    @Override
    public void logout(String token) {
        // 将token加入黑名单
        if (redisTemplate != null && StrUtil.isNotBlank(token)) {
            Long expireTime = jwtService.getExpireTimeFromToken(token);
            if (expireTime > 0) {
                redisTemplate.opsForValue().set(
                        "admin:token:blacklist:" + token,
                        "1",
                        expireTime,
                        TimeUnit.SECONDS
                );
            }
        }
    }

    // ========== 私有方法 ==========

    private List<SysRoleDO> getUserRoles(Long userId) {
        // 查询用户角色
        String sql = """
                SELECT r.* FROM sys_role r
                INNER JOIN sys_user_role ur ON r.id = ur.role_id
                WHERE ur.user_id = #{userId} AND r.status = 1 AND r.deleted = 0
                """;
        // 使用MyBatis-Plus查询
        return roleMapper.selectList(
                new LambdaQueryWrapper<SysRoleDO>()
                        .inSql(SysRoleDO::getId,
                                "SELECT role_id FROM sys_user_role WHERE user_id = " + userId)
                        .eq(SysRoleDO::getStatus, 1)
        );
    }

    private List<SysPermissionDO> getUserPermissions(Long userId) {
        // 判断是否超管
        List<SysRoleDO> roles = getUserRoles(userId);
        boolean isSuperAdmin = roles.stream()
                .anyMatch(r -> "SUPER_ADMIN".equals(r.getCode()));

        if (isSuperAdmin) {
            // 超管拥有所有权限
            return permissionMapper.selectList(
                    new LambdaQueryWrapper<SysPermissionDO>()
                            .eq(SysPermissionDO::getStatus, 1)
            );
        }

        return permissionMapper.selectByUserId(userId);
    }

    private AdminUserVO buildUserVO(SysAdminDO admin, List<SysRoleDO> roles, List<SysPermissionDO> permissions) {
        AdminUserVO vo = BeanUtil.copyProperties(admin, AdminUserVO.class);
        vo.setRoles(roles.stream().map(SysRoleDO::getCode).collect(Collectors.toList()));
        vo.setPermissions(permissions.stream()
                .map(SysPermissionDO::getCode)
                .collect(Collectors.toList()));
        vo.setIsSuperAdmin(roles.stream().anyMatch(r -> "SUPER_ADMIN".equals(r.getCode())));
        vo.setUserType(UserType.ADMIN);
        return vo;
    }
}
