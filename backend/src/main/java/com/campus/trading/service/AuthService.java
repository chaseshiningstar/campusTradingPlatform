package com.campus.trading.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trading.dto.LoginRequest;
import com.campus.trading.dto.LoginResponse;
import com.campus.trading.dto.RegisterRequest;
import com.campus.trading.entity.SysRole;
import com.campus.trading.entity.SysUser;
import com.campus.trading.mapper.SysRoleMapper;
import com.campus.trading.mapper.SysUserMapper;
import com.campus.trading.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        SysUser user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 获取用户角色
        List<Long> roleIds = userMapper.selectRoleIdsByUserId(user.getId());
        String role = "USER";
        if (!roleIds.isEmpty()) {
            SysRole sysRole = roleMapper.selectById(roleIds.get(0));
            if (sysRole != null) {
                role = sysRole.getRoleCode();
            }
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);

        // 构建响应
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                role
        );

        return new LoginResponse(token, userInfo);
    }

    /**
     * 发送注册验证码
     */
    public void sendRegisterCode(String email) {
        // 检查邮箱是否已被注册
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getEmail, email);
        Long existCount = userMapper.selectCount(wrapper);
        if (existCount > 0) {
            throw new RuntimeException("该邮箱已被注册,请直接登录");
        }
        emailService.sendRegisterCode(email);
    }

    /**
     * 用户注册(需校验邮箱验证码)
     */
    @Transactional
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        SysUser existUser = userMapper.selectByUsername(request.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已被注册
        LambdaQueryWrapper<SysUser> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(SysUser::getEmail, request.getEmail());
        Long emailCount = userMapper.selectCount(emailWrapper);
        if (emailCount > 0) {
            throw new RuntimeException("该邮箱已被注册");
        }

        // 校验邮箱验证码
        if (!emailService.verifyCode(request.getEmail(), request.getEmailCode())) {
            throw new RuntimeException("邮箱验证码错误或已过期");
        }

        // 创建新用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1);

        userMapper.insert(user);

        // 分配普通用户角色
        SysRole userRole = roleMapper.selectByRoleCode("USER");
        if (userRole != null) {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getUsername, request.getUsername());
            SysUser newUser = userMapper.selectOne(wrapper);

            // 插入用户角色关联(使用原生SQL)
            userMapper.insertUserRole(newUser.getId(), userRole.getId());
        }
    }
}
