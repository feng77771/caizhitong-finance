package com.finance.controller;

import com.finance.dto.ApiResponse;
import com.finance.entity.User;
import com.finance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@CrossOrigin
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<User>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取用户列表失败: " + e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<User> createUser(@RequestBody Map<String, Object> userData) {
        try {
            User user = new User();
            user.setUsername((String) userData.get("username"));
            user.setEmail((String) userData.get("email"));
            user.setPhone((String) userData.get("phone"));
            user.setPassword((String) userData.get("password"));
            user.setRole((String) userData.get("role"));
            
            User createdUser = userService.createUser(user);
            return ApiResponse.success(createdUser);
        } catch (Exception e) {
            return ApiResponse.error(500, "创建用户失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userData) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return ApiResponse.error(404, "用户不存在");
            }
            
            user.setUsername((String) userData.get("username"));
            user.setEmail((String) userData.get("email"));
            user.setPhone((String) userData.get("phone"));
            user.setRole((String) userData.get("role"));
            user.setStatus((String) userData.get("status"));
            
            User updatedUser = userService.updateUser(user);
            return ApiResponse.success(updatedUser);
        } catch (Exception e) {
            return ApiResponse.error(500, "更新用户失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(500, "删除用户失败: " + e.getMessage());
        }
    }
}