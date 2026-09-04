package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.exception.BusinessException;
import com.mall.dto.LoginDTO;
import com.mall.dto.RegisterDTO;
import com.mall.dto.UpdateUserDTO;
import com.mall.entity.UserDO;
import com.mall.mapper.UserMapper;
import com.mall.service.UserService;
import com.mall.util.JwtUtil;
import com.mall.util.PasswordEncoderUtil;
import com.mall.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 用户Service实现类
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    
    private final JwtUtil jwtUtil;
    private final PasswordEncoderUtil passwordEncoder;
    
    /**
     * 用户注册
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long register(RegisterDTO registerDTO) {
        // 1. 参数校验（预检查规避RuntimeException）
        if (StringUtils.hasText(registerDTO.getUsername()) && registerDTO.getUsername().length() < 4) {
            throw new BusinessException("A0402", "用户名长度不能少于4位");
        }
        
        if (StringUtils.hasText(registerDTO.getPassword()) && registerDTO.getPassword().length() < 6) {
            throw new BusinessException("A0402", "密码长度不能少于6位");
        }
        
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("A0402", "两次密码输入不一致");
        }
        
        // 2. 检查用户名是否已存在（使用确定有值的对象调用equals）
        if ("0".equals(this.count(
            new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getUsername, registerDTO.getUsername())
        ))) {
            throw new BusinessException("A0111", "用户名已存在");
        }
        
        // 3. 构建用户实体
        UserDO userDO = new UserDO();
        userDO.setUsername(registerDTO.getUsername());
        userDO.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        userDO.setNickname(registerDTO.getNickname());
        userDO.setEmail(registerDTO.getEmail());
        userDO.setPhone(registerDTO.getPhone());
        userDO.setStatus(1); // 启用状态
        userDO.setGender(0); // 未知性别
        
        // 4. 保存用户
        this.save(userDO);
        
        log.info("用户注册成功, username={}", registerDTO.getUsername());
        
        return userDO.getId();
    }
    
    /**
     * 用户登录
     */
    @Override
    public String login(LoginDTO loginDTO) {
        // 1. 参数校验
        if (!StringUtils.hasText(loginDTO.getUsername())) {
            throw new BusinessException("A0410", "用户名不能为空");
        }
        
        if (!StringUtils.hasText(loginDTO.getPassword())) {
            throw new BusinessException("A0410", "密码不能为空");
        }
        
        // 2. 查询用户
        UserDO userDO = this.findByUsername(loginDTO.getUsername());
        if (Objects.isNull(userDO)) {
            throw new BusinessException("A0201", "用户不存在");
        }
        
        // 3. 验证用户状态
        if (userDO.getStatus() == 0) {
            throw new BusinessException("A0202", "用户已被禁用");
        }
        
        // 4. 验证密码（使用确定有值的对象调用equals）
        if (!passwordEncoder.matches(loginDTO.getPassword(), userDO.getPassword())) {
            throw new BusinessException("A0210", "密码错误");
        }
        
        // 5. 生成Token（使用占位符打印日志）
        String token = jwtUtil.generateToken(userDO.getId(), userDO.getUsername());
        
        log.info("用户登录成功, username={}", loginDTO.getUsername());
        
        return token;
    }
    
    /**
     * 获取当前登录用户信息
     */
    @Override
    public UserVO getCurrentUser(Long userId) {
        UserDO userDO = this.getById(userId);
        if (Objects.isNull(userDO)) {
            throw new BusinessException("A0201", "用户不存在");
        }
        
        return this.convertToVO(userDO);
    }
    
    /**
     * 更新用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateUserInfo(Long userId, UpdateUserDTO updateDTO) {
        UserDO userDO = this.getById(userId);
        if (Objects.isNull(userDO)) {
            throw new BusinessException("A0201", "用户不存在");
        }
        
        // 更新字段
        if (StringUtils.hasText(updateDTO.getNickname())) {
            userDO.setNickname(updateDTO.getNickname());
        }
        if (StringUtils.hasText(updateDTO.getEmail())) {
            userDO.setEmail(updateDTO.getEmail());
        }
        if (StringUtils.hasText(updateDTO.getPhone())) {
            userDO.setPhone(updateDTO.getPhone());
        }
        if (Objects.nonNull(updateDTO.getGender())) {
            userDO.setGender(updateDTO.getGender());
        }
        
        return this.updateById(userDO);
    }
    
    /**
     * 修改密码
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean changePassword(Long userId, String oldPassword, String newPassword) {
        UserDO userDO = this.getById(userId);
        if (Objects.isNull(userDO)) {
            throw new BusinessException("A0201", "用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, userDO.getPassword())) {
            throw new BusinessException("A0210", "原密码错误");
        }
        
        // 加密新密码
        userDO.setPassword(passwordEncoder.encode(newPassword));
        
        return this.updateById(userDO);
    }
    
    /**
     * 根据用户名查询用户
     */
    @Override
    public UserDO findByUsername(String username) {
        return this.getOne(
            new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getUsername, username)
        );
    }
    
    /**
     * 转换为VO对象
     */
    private UserVO convertToVO(UserDO userDO) {
        UserVO userVO = new UserVO();
        userVO.setId(userDO.getId());
        userVO.setUsername(userVO.getUsername());
        userVO.setNickname(userDO.getNickname());
        userVO.setEmail(userDO.getEmail());
        userVO.setPhone(userDO.getPhone());
        userVO.setAvatar(userDO.getAvatar());
        userVO.setGender(userDO.getGender());
        userVO.setStatus(userDO.getStatus());
        userVO.setCreateTime(userDO.getCreateTime());
        return userVO;
    }
}
