package com.mall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.enums.UserType;
import com.mall.common.exception.BusinessException;
import com.mall.entity.SysAdminDO;
import com.mall.entity.SysPermissionDO;
import com.mall.entity.SysRoleDO;
import com.mall.mapper.SysAdminMapper;
import com.mall.mapper.SysPermissionMapper;
import com.mall.mapper.SysRoleMapper;
import com.mall.service.AdminUserService;
import com.mall.vo.AdminUserVO;
import com.mall.vo.MenuVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 后台用户服务实现类
 *
 * @author mall
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final SysAdminMapper adminMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;

    @Override
    public AdminUserVO getUserInfo(Long userId) {
        SysAdminDO admin = adminMapper.selectById(userId);
        if (admin == null) {
            throw BusinessException.of("A0401", "用户不存在");
        }

        List<SysRoleDO> roles = getUserRoles(userId);
        List<SysPermissionDO> permissions = getUserPermissions(userId);

        return buildUserVO(admin, roles, permissions);
    }

    @Override
    public List<MenuVO> getUserMenus(Long userId) {
        List<SysPermissionDO> permissions = getUserPermissions(userId);

        // 只获取菜单类型的权限
        List<SysPermissionDO> menus = permissions.stream()
                .filter(p -> p.getPermissionType() == 1) // 1=菜单
                .collect(Collectors.toList());

        // 构建树形结构
        return buildMenuTree(menus);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        List<SysPermissionDO> permissions = getUserPermissions(userId);
        return permissions.stream()
                .map(SysPermissionDO::getCode)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean changePassword(Long userId, String oldPassword, String newPassword) {
        SysAdminDO admin = adminMapper.selectById(userId);
        if (admin == null) {
            throw BusinessException.of("A0401", "用户不存在");
        }

        // 验证旧密码
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(oldPassword, admin.getPassword())) {
            throw BusinessException.of("A0401", "原密码错误");
        }

        // 更新密码
        admin.setPassword(encoder.encode(newPassword));
        return adminMapper.updateById(admin) > 0;
    }

    // ========== 私有方法 ==========

    private List<SysRoleDO> getUserRoles(Long userId) {
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

    private List<MenuVO> buildMenuTree(List<SysPermissionDO> permissions) {
        Map<Long, List<SysPermissionDO>> groupByParent = permissions.stream()
                .collect(Collectors.groupingBy(SysPermissionDO::getParentId));

        // 递归构建树
        return buildMenuChildren(0L, groupByParent);
    }

    private List<MenuVO> buildMenuChildren(Long parentId, Map<Long, List<SysPermissionDO>> groupByParent) {
        List<MenuVO> result = new ArrayList<>();

        List<SysPermissionDO> children = groupByParent.get(parentId);
        if (children == null || children.isEmpty()) {
            return result;
        }

        for (SysPermissionDO permission : children) {
            MenuVO menuVO = new MenuVO();
            menuVO.setId(permission.getId());
            menuVO.setParentId(permission.getParentId());
            menuVO.setName(permission.getName());
            menuVO.setCode(permission.getCode());
            menuVO.setPath(permission.getPath());
            menuVO.setIcon(permission.getIcon());
            menuVO.setSort(permission.getSort());

            // 递归构建子菜单
            List<MenuVO> subMenus = buildMenuChildren(permission.getId(), groupByParent);
            menuVO.setChildren(subMenus);

            result.add(menuVO);
        }

        // 排序
        result.sort((a, b) -> {
            if (a.getSort() == null) a.setSort(0);
            if (b.getSort() == null) b.setSort(0);
            return a.getSort().compareTo(b.getSort());
        });

        return result;
    }
}
