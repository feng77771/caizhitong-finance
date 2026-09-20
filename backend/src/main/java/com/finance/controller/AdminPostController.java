package com.finance.controller;

import com.finance.dto.ApiResponse;
import com.finance.entity.ForumPost;
import com.finance.service.ForumPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/posts")
@CrossOrigin
public class AdminPostController {

    @Autowired
    private ForumPostService forumPostService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ForumPost>> getAllPosts() {
        try {
            List<ForumPost> posts = forumPostService.getAllPosts();
            return ApiResponse.success(posts);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取帖子列表失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updatePostStatus(@PathVariable Long id, @RequestBody Map<String, String> statusData) {
        try {
            String status = statusData.get("status");
            forumPostService.updatePostStatus(id, status);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(500, "更新帖子状态失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deletePost(@PathVariable Long id) {
        try {
            forumPostService.deletePost(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(500, "删除帖子失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDeletePosts(@RequestBody Map<String, List<Long>> data) {
        try {
            List<Long> ids = data.get("ids");
            forumPostService.batchDeletePosts(ids);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(500, "批量删除帖子失败: " + e.getMessage());
        }
    }
}