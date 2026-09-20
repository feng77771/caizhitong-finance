package com.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.finance.dto.LoginRequest;
import com.finance.dto.RegisterRequest;
import com.finance.entity.User;
import com.finance.mapper.UserMapper;
import com.finance.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public Map<String, Object> register(RegisterRequest request) {
        // 验证密码确认
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次密码输入不一致");
        }
        
        // 检查用户名是否存在
        QueryWrapper<User> usernameQuery = new QueryWrapper<>();
        usernameQuery.eq("username", request.getUsername());
        if (userMapper.selectOne(usernameQuery) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否存在
        QueryWrapper<User> emailQuery = new QueryWrapper<>();
        emailQuery.eq("email", request.getEmail());
        if (userMapper.selectOne(emailQuery) != null) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        // 检查手机号是否存在
        QueryWrapper<User> phoneQuery = new QueryWrapper<>();
        phoneQuery.eq("phone", request.getPhone());
        if (userMapper.selectOne(phoneQuery) != null) {
            throw new RuntimeException("手机号已被注册");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("user"); // 默认设置为普通用户
        
        userMapper.insert(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        return result;
    }
    
    public Map<String, Object> login(LoginRequest request) {
        // 根据用户名/邮箱/手机号查找用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getLoginName())
                   .or()
                   .eq("email", request.getLoginName())
                   .or()
                   .eq("phone", request.getLoginName());
        
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 检查用户状态
        if ("disabled".equals(user.getStatus())) {
            throw new RuntimeException("用户已被禁用，无法登录");
        }
        
        // 获取用户的真实角色
        String userRole = user.getRole();
        if (userRole == null || userRole.isEmpty()) {
            userRole = "user"; // 默认为普通用户
        }
        
        // 获取用户选择的角色（前端传入）
        String selectedRole = request.getRole();
        if (selectedRole == null || selectedRole.isEmpty()) {
            selectedRole = "user"; // 默认选择普通用户
        }
        
        // 验证用户选择的角色与数据库中的角色是否匹配
        if (!userRole.equals(selectedRole)) {
            throw new RuntimeException("角色不匹配，请选择正确的角色登录");
        }
        
        // 生成JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone());
        result.put("role", userRole); // 返回用户的真实角色
        result.put("registerTime", user.getRegisterTime()); // 返回注册时间
        return result;
    }
    
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
    
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }
    
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("user");
        }
        if (user.getStatus() == null || user.getStatus().isEmpty()) {
            user.setStatus("active");
        }
        userMapper.insert(user);
        return user;
    }
    
    public User updateUser(User user) {
        userMapper.updateById(user);
        return user;
    }
    
    public void deleteUser(Long userId) {
        userMapper.deleteById(userId);
    }
    
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        // 更新新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
