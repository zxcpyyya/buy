package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.SysPermissionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper
 *
 * @author mall
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermissionDO> {

    /**
     * 根据角色ID查询权限
     */
    @Select("""
            SELECT p.* FROM sys_permission p
            INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
            WHERE rp.role_id = #{roleId} AND p.status = 1 AND p.deleted = 0
            """)
    List<SysPermissionDO> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询权限（通过角色）
     */
    @Select("""
            SELECT DISTINCT p.* FROM sys_permission p
            INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
            INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id
            WHERE ur.user_id = #{userId} AND p.status = 1 AND p.deleted = 0
            """)
    List<SysPermissionDO> selectByUserId(@Param("userId") Long userId);
}
