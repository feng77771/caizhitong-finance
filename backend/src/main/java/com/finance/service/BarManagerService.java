package com.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.finance.dto.ApiResponse;
import com.finance.entity.BarManager;
import com.finance.entity.BarManagerApplication;
import com.finance.entity.ForumPost;
import com.finance.entity.User;
import com.finance.mapper.BarManagerApplicationMapper;
import com.finance.mapper.BarManagerMapper;
import com.finance.mapper.ForumCommentMapper;
import com.finance.mapper.ForumLikeMapper;
import com.finance.mapper.ForumPostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BarManagerService {
    
    @Autowired
    private BarManagerApplicationMapper applicationMapper;
    
    @Autowired
    private BarManagerMapper barManagerMapper;
    
    @Autowired
    private ForumPostMapper forumPostMapper;
    
    @Autowired
    private ForumCommentMapper forumCommentMapper;
    
    @Autowired
    private ForumLikeMapper forumLikeMapper;
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            return user.getId();
        }
        throw new RuntimeException("用户未登录");
    }
    
    @Transactional
    public ApiResponse<String> applyManager(BarManagerApplication application) {
        Long userId = getCurrentUserId();
        
        Integer exists = barManagerMapper.countByUserAndCategory(userId, application.getCategory());
        if (exists != null && exists > 0) {
            return ApiResponse.error("您已经是该贴吧的主理人");
        }
        
        QueryWrapper<BarManagerApplication> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("category", application.getCategory());
        wrapper.eq("status", "pending");
        if (applicationMapper.selectCount(wrapper) > 0) {
            return ApiResponse.error("您已经申请过该贴吧的主理人，请等待审核");
        }
        
        application.setUserId(userId);
        application.setStatus("pending");
        application.setCreateTime(LocalDateTime.now());
        applicationMapper.insert(application);
        return ApiResponse.success("申请已提交，请等待管理员审核");
    }
    
    public ApiResponse<List<BarManagerApplication>> getMyApplications() {
        Long userId = getCurrentUserId();
        List<BarManagerApplication> applications = applicationMapper.selectByUserId(userId);
        return ApiResponse.success(applications);
    }
    
    public ApiResponse<List<BarManagerApplication>> getPendingApplications() {
        List<BarManagerApplication> applications = applicationMapper.selectByStatus("pending");
        return ApiResponse.success(applications);
    }
    
    @Transactional
    public ApiResponse<String> reviewApplication(Long applicationId, String status, String reviewComment) {
        try {
            BarManagerApplication application = applicationMapper.selectById(applicationId);
            if (application == null) {
                return ApiResponse.error("申请不存在");
            }
            
            if (!"approved".equals(status) && !"rejected".equals(status)) {
                return ApiResponse.error("无效的审核状态");
            }
            
            Long currentUserId = getCurrentUserId();
            application.setStatus(status);
            application.setReviewUserId(currentUserId);
            application.setReviewComment(reviewComment);
            application.setReviewTime(LocalDateTime.now());
            applicationMapper.updateById(application);
            
            if ("approved".equals(status)) {
                // 检查是否已经是主理人
                Integer exists = barManagerMapper.countByUserAndCategory(application.getUserId(), application.getCategory());
                if (exists == null || exists == 0) {
                    BarManager barManager = new BarManager();
                    barManager.setUserId(application.getUserId());
                    barManager.setCategory(application.getCategory());
                    barManager.setStartTime(LocalDateTime.now());
                    barManager.setStatus("active");
                    barManagerMapper.insert(barManager);
                }
            }
            
            return ApiResponse.success("审核完成");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("审核失败：" + e.getMessage());
        }
    }
    
    public ApiResponse<List<BarManager>> getAllManagers() {
        List<BarManager> managers = barManagerMapper.selectAllActive();
        return ApiResponse.success(managers);
    }
    
    public ApiResponse<List<BarManager>> getManagersByCategory(String category) {
        List<BarManager> managers = barManagerMapper.selectByCategory(category);
        return ApiResponse.success(managers);
    }
    
    public ApiResponse<Boolean> isManager(String category) {
        Long userId = getCurrentUserId();
        Integer count = barManagerMapper.countByUserAndCategory(userId, category);
        return ApiResponse.success(count != null && count > 0);
    }
    
    public ApiResponse<List<BarManager>> getMyManagedCategories() {
        Long userId = getCurrentUserId();
        List<BarManager> categories = barManagerMapper.selectByUserId(userId);
        return ApiResponse.success(categories);
    }
    
    public ApiResponse<List<ForumPost>> getPostsByManagedCategory(String category) {
        Long userId = getCurrentUserId();
        
        Integer count = barManagerMapper.countByUserAndCategory(userId, category);
        if (count == null || count == 0) {
            return ApiResponse.error("您没有管理该贴吧的权限");
        }
        
        List<ForumPost> posts = forumPostMapper.selectByCategory(category);
        return ApiResponse.success(posts);
    }
    
    @Transactional
    public ApiResponse<String> deletePost(Long postId) {
        Long userId = getCurrentUserId();
        
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null) {
            return ApiResponse.error("帖子不存在");
        }
        
        String category = post.getCategory();
        Integer count = barManagerMapper.countByUserAndCategory(userId, category);
        if (count == null || count == 0) {
            return ApiResponse.error("您没有删除该帖子的权限");
        }
        
        forumCommentMapper.deleteByPostId(postId);
        forumLikeMapper.deleteByPostId(postId);
        forumPostMapper.deleteById(postId);
        
        return ApiResponse.success("删除成功");
    }
    
    @Transactional
    public ApiResponse<String> updatePostStatus(Long postId, String status) {
        Long userId = getCurrentUserId();
        
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null) {
            return ApiResponse.error("帖子不存在");
        }
        
        String category = post.getCategory();
        Integer count = barManagerMapper.countByUserAndCategory(userId, category);
        if (count == null || count == 0) {
            return ApiResponse.error("您没有修改该帖子的权限");
        }
        
        post.setStatus(status);
        forumPostMapper.updateById(post);
        
        return ApiResponse.success("状态更新成功");
    }
    
    @Transactional
    public ApiResponse<String> editPost(Long postId, String title, String content) {
        Long userId = getCurrentUserId();
        
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null) {
            return ApiResponse.error("帖子不存在");
        }
        
        String category = post.getCategory();
        Integer count = barManagerMapper.countByUserAndCategory(userId, category);
        if (count == null || count == 0) {
            return ApiResponse.error("您没有编辑该帖子的权限");
        }
        
        post.setTitle(title);
        post.setContent(content);
        forumPostMapper.updateById(post);
        
        return ApiResponse.success("编辑成功");
    }
    
    @Transactional
    public ApiResponse<String> managePost(Long postId, String action, String reason) {
        Long userId = getCurrentUserId();
        
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null) {
            return ApiResponse.error("帖子不存在");
        }
        
        String category = post.getCategory();
        Integer count = barManagerMapper.countByUserAndCategory(userId, category);
        if (count == null || count == 0) {
            return ApiResponse.error("您没有管理该帖子的权限");
        }
        
        if ("delete".equals(action)) {
            forumCommentMapper.deleteByPostId(postId);
            forumLikeMapper.deleteByPostId(postId);
            forumPostMapper.deleteById(postId);
            return ApiResponse.success("帖子已删除");
        } else if ("hide".equals(action)) {
            post.setStatus("hidden");
            forumPostMapper.updateById(post);
            return ApiResponse.success("帖子已隐藏");
        } else if ("show".equals(action)) {
            post.setStatus("normal");
            forumPostMapper.updateById(post);
            return ApiResponse.success("帖子已显示");
        }
        
        return ApiResponse.error("无效的操作");
    }
    
    @Transactional
    public ApiResponse<String> removeManager(Long managerId) {
        int result = barManagerMapper.deleteById(managerId);
        if (result == 0) {
            return ApiResponse.error("主理人不存在");
        }
        return ApiResponse.success("删除成功");
    }
}
