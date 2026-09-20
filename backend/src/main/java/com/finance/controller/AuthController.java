package com.finance.controller;

import com.finance.dto.ApiResponse;
import com.finance.dto.LoginRequest;
import com.finance.dto.RegisterRequest;
import com.finance.service.CaptchaService;
import com.finance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CaptchaService captchaService;
    
    @Autowired
    private com.finance.util.JwtUtil jwtUtil;
    
    @GetMapping("/captcha")
    public ApiResponse<Map<String, String>> getCaptcha() {
        try {
            Map<String, String> captcha = captchaService.generateCaptcha();
            return ApiResponse.success(captcha);
        } catch (Exception e) {
            return ApiResponse.error("验证码生成失败");
        }
    }
    
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Validated @RequestBody RegisterRequest request) {
        try {
            Map<String, Object> result = userService.register(request);
            return ApiResponse.success("注册成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Validated @RequestBody LoginRequest request) {
        try {
            // 先验证验证码
            if (!captchaService.validateCaptcha(request.getUuid(), request.getCaptcha())) {
                return ApiResponse.error("验证码错误");
            }
            
            Map<String, Object> result = userService.login(request);
            return ApiResponse.success("登录成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/change-password")
    public ApiResponse<String> changePassword(@RequestBody Map<String, Object> request, 
                                              @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = null;
            
            // 尝试从JWT token中获取用户ID
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                userId = jwtUtil.extractUserId(jwtToken);
            }
            
            // 如果从token中获取失败，尝试从请求体中获取
            if (userId == null && request.containsKey("userId")) {
                userId = Long.parseLong(request.get("userId").toString());
            }
            
            if (userId == null) {
                return ApiResponse.error("无法获取用户信息");
            }
            
            String oldPassword = request.get("oldPassword").toString();
            String newPassword = request.get("newPassword").toString();
            
            userService.updatePassword(userId, oldPassword, newPassword);
            return ApiResponse.success("密码修改成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}