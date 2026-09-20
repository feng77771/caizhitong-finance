package com.finance.controller;

import com.finance.dto.ApiResponse;
import com.finance.entity.BarManager;
import com.finance.entity.BarManagerApplication;
import com.finance.entity.ForumPost;
import com.finance.service.BarManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bar-manager")
public class BarManagerController {
    
    @Autowired
    private BarManagerService barManagerService;
    
    @PostMapping("/apply")
    public ApiResponse<String> applyManager(@RequestBody BarManagerApplication application) {
        return barManagerService.applyManager(application);
    }
    
    @GetMapping("/my-applications")
    public ApiResponse<List<BarManagerApplication>> getMyApplications() {
        return barManagerService.getMyApplications();
    }
    
    @GetMapping("/pending-applications")
    public ApiResponse<List<BarManagerApplication>> getPendingApplications() {
        return barManagerService.getPendingApplications();
    }
    
    @PostMapping("/review")
    public ApiResponse<String> reviewApplication(@RequestBody Map<String, Object> request) {
        try {
            Long applicationId = Long.valueOf(request.get("applicationId").toString());
            String status = request.get("status").toString();
            String reviewComment = request.get("reviewComment") != null ? request.get("reviewComment").toString() : "";
            return barManagerService.reviewApplication(applicationId, status, reviewComment);
        } catch (NumberFormatException e) {
            return ApiResponse.error("无效的申请ID");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("审核失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/all")
    public ApiResponse<List<BarManager>> getAllManagers() {
        return barManagerService.getAllManagers();
    }
    
    @GetMapping("/category/{category}")
    public ApiResponse<List<BarManager>> getManagersByCategory(@PathVariable String category) {
        return barManagerService.getManagersByCategory(category);
    }
    
    @GetMapping("/check/{category}")
    public ApiResponse<Boolean> isManager(@PathVariable String category) {
        return barManagerService.isManager(category);
    }
    
    @PostMapping("/manage-post")
    public ApiResponse<String> managePost(@RequestBody Map<String, Object> request) {
        Long postId = Long.valueOf(request.get("postId").toString());
        String action = request.get("action").toString();
        String reason = request.get("reason") != null ? request.get("reason").toString() : "";
        return barManagerService.managePost(postId, action, reason);
    }
    
    @GetMapping("/my-managed-categories")
    public ApiResponse<List<BarManager>> getMyManagedCategories() {
        return barManagerService.getMyManagedCategories();
    }
    
    @GetMapping("/posts/{category}")
    public ApiResponse<List<ForumPost>> getPostsByManagedCategory(@PathVariable String category) {
        return barManagerService.getPostsByManagedCategory(category);
    }
    
    @PutMapping("/post/{id}")
    public ApiResponse<String> editPost(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String title = request.get("title") != null ? request.get("title").toString() : "";
        String content = request.get("content") != null ? request.get("content").toString() : "";
        return barManagerService.editPost(id, title, content);
    }
    
    @DeleteMapping("/post/{id}")
    public ApiResponse<String> deletePost(@PathVariable Long id) {
        return barManagerService.deletePost(id);
    }
    
    @PutMapping("/post/{id}/status")
    public ApiResponse<String> updatePostStatus(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String status = request.get("status").toString();
        return barManagerService.updatePostStatus(id, status);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<String> removeManager(@PathVariable Long id) {
        return barManagerService.removeManager(id);
    }
}
