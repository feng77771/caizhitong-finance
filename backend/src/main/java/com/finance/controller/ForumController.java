package com.finance.controller;

import com.finance.dto.ApiResponse;
import com.finance.entity.ForumComment;
import com.finance.entity.ForumPost;
import com.finance.entity.User;
import com.finance.service.ForumPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/forum")
public class ForumController {
    
    @Autowired
    private ForumPostService forumPostService;
    
    @GetMapping("/posts/latest")
    public ApiResponse<List<ForumPost>> getLatestPosts(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String category) {
        List<ForumPost> posts = forumPostService.getLatestPosts(limit, category);
        processAnonymousPosts(posts);
        return ApiResponse.success(posts);
    }
    
    @GetMapping("/posts/hot")
    public ApiResponse<List<ForumPost>> getHotPosts(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String category) {
        List<ForumPost> posts = forumPostService.getHotPosts(limit, category);
        processAnonymousPosts(posts);
        return ApiResponse.success(posts);
    }
    
    @GetMapping("/posts/{id}/comments")
    public ApiResponse<List<ForumComment>> getCommentsByPostId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<ForumComment> comments = forumPostService.getCommentsByPostId(id, limit);
        processAnonymousComments(comments);
        return ApiResponse.success(comments);
    }
    
    private void processAnonymousPosts(List<ForumPost> posts) {
        boolean isAdmin = isCurrentUserAdmin();
        for (ForumPost post : posts) {
            if (post.getIsAnonymous() != null && post.getIsAnonymous() == 1 && !isAdmin) {
                post.setDisplayName("匿名用户");
            } else {
                post.setDisplayName(post.getUsername());
            }
        }
    }
    
    private void processAnonymousComments(List<ForumComment> comments) {
        boolean isAdmin = isCurrentUserAdmin();
        for (ForumComment comment : comments) {
            if (comment.getIsAnonymous() != null && comment.getIsAnonymous() == 1 && !isAdmin) {
                comment.setDisplayName("匿名用户");
            } else {
                comment.setDisplayName(comment.getUsername());
            }
        }
    }
    
    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            return "admin".equals(user.getRole());
        }
        return false;
    }
    
    @GetMapping("/posts/{id}")
    public ApiResponse<ForumPost> getPostById(@PathVariable Long id) {
        ForumPost post = forumPostService.getPostById(id);
        return post != null ? ApiResponse.success(post) : ApiResponse.error("帖子不存在");
    }
    
    @GetMapping("/posts/liked")
    public ApiResponse<List<ForumPost>> getLikedPosts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            List<ForumPost> posts = forumPostService.getLikedPostsByUserId(user.getId());
            processAnonymousPosts(posts);
            return ApiResponse.success(posts);
        }
        return ApiResponse.error("用户未登录");
    }
    
    @PostMapping("/posts/{id}/view")
    public ApiResponse<String> viewPost(@PathVariable Long id) {
        boolean success = forumPostService.viewPost(id);
        return success ? ApiResponse.success("浏览成功") : ApiResponse.error("浏览失败");
    }
    
    @PostMapping("/posts/{id}/like")
    public ApiResponse<String> likePost(@PathVariable Long id) {
        boolean success = forumPostService.likePost(id);
        return success ? ApiResponse.success("操作成功") : ApiResponse.error("操作失败");
    }
    
    @PostMapping("/posts/{id}/comments")
    public ApiResponse<String> addComment(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String content = (String) request.get("content");
        Integer isAnonymous = request.get("isAnonymous") != null ? ((Number) request.get("isAnonymous")).intValue() : 0;
        boolean success = forumPostService.addComment(id, content, isAnonymous);
        return success ? ApiResponse.success("评论成功") : ApiResponse.error("评论失败");
    }
    
    @PostMapping("/posts")
    public ApiResponse<String> createPost(@RequestBody Map<String, Object> request) {
        ForumPost post = new ForumPost();
        post.setTitle((String) request.get("title"));
        post.setCategory((String) request.get("category"));
        post.setContent((String) request.get("content"));
        post.setIsAnonymous(request.get("isAnonymous") != null ? ((Number) request.get("isAnonymous")).intValue() : 0);
        
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            post.setUserId(user.getId());
        } else {
            // 默认用户ID为1（admin）
            post.setUserId(1L);
        }
        
        boolean success = forumPostService.createPost(post);
        return success ? ApiResponse.success("发布成功") : ApiResponse.error("发布失败");
    }
}
