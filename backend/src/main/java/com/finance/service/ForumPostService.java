package com.finance.service;

import com.finance.entity.ForumComment;
import com.finance.entity.ForumLike;
import com.finance.entity.ForumPost;
import com.finance.entity.User;
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
public class ForumPostService {
    
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
        return 1L; // 默认用户ID
    }
    
    public List<ForumPost> getLatestPosts(Integer limit, String category) {
        return forumPostMapper.selectLatestPosts(limit != null ? limit : 10, category);
    }
    
    public List<ForumPost> getHotPosts(Integer limit, String category) {
        return forumPostMapper.selectHotPosts(limit != null ? limit : 10, category);
    }
    
    public ForumPost getPostById(Long id) {
        return forumPostMapper.selectById(id);
    }
    
    @Transactional
    public boolean createPost(ForumPost post) {
        post.setViews(0);
        post.setLikes(0);
        post.setIsAnonymous(post.getIsAnonymous() == null ? 0 : post.getIsAnonymous());
        post.setStatus("visible");
        post.setCreateTime(LocalDateTime.now());
        return forumPostMapper.insert(post) > 0;
    }
    
    @Transactional
    public boolean viewPost(Long id) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post != null) {
            post.setViews(post.getViews() + 1);
            forumPostMapper.updateById(post);
            return true;
        }
        return false;
    }
    
    @Transactional
    public boolean likePost(Long id) {
        Long userId = getCurrentUserId();
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) {
            return false;
        }
        
        int exists = forumLikeMapper.countByPostIdAndUserId(id, userId);
        if (exists > 0) {
            // 取消点赞
            forumLikeMapper.deleteByPostIdAndUserId(id, userId);
            post.setLikes(post.getLikes() - 1);
        } else {
            // 添加点赞
            ForumLike like = new ForumLike();
            like.setPostId(id);
            like.setUserId(userId);
            like.setCreateTime(LocalDateTime.now());
            forumLikeMapper.insert(like);
            post.setLikes(post.getLikes() + 1);
        }
        forumPostMapper.updateById(post);
        return true;
    }
    
    @Transactional
    public boolean addComment(Long postId, String content, Integer isAnonymous) {
        Long userId = getCurrentUserId();
        ForumComment comment = new ForumComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setIsAnonymous(isAnonymous == null ? 0 : isAnonymous);
        comment.setCreateTime(LocalDateTime.now());
        
        // 插入评论
        int result = forumCommentMapper.insert(comment);
        return result > 0;
    }
    
    @Transactional
    public boolean addComment(Long postId, Long userId, String content, Integer isAnonymous) {
        ForumComment comment = new ForumComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setIsAnonymous(isAnonymous == null ? 0 : isAnonymous);
        comment.setCreateTime(LocalDateTime.now());
        return forumCommentMapper.insert(comment) > 0;
    }
    
    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        int exists = forumLikeMapper.countByPostIdAndUserId(postId, userId);
        if (exists > 0) {
            // 已点赞，取消点赞
            forumLikeMapper.deleteByPostIdAndUserId(postId, userId);
            // 更新帖子点赞数
            ForumPost post = forumPostMapper.selectById(postId);
            if (post != null) {
                post.setLikes(post.getLikes() - 1);
                forumPostMapper.updateById(post);
            }
            return false; // 返回false表示已取消点赞
        } else {
            // 未点赞，添加点赞
            ForumLike like = new ForumLike();
            like.setPostId(postId);
            like.setUserId(userId);
            like.setCreateTime(LocalDateTime.now());
            forumLikeMapper.insert(like);
            // 更新帖子点赞数
            ForumPost post = forumPostMapper.selectById(postId);
            if (post != null) {
                post.setLikes(post.getLikes() + 1);
                forumPostMapper.updateById(post);
            }
            return true; // 返回true表示已点赞
        }
    }
    
    public Integer getCommentCount(Long postId) {
        return forumCommentMapper.countByPostId(postId);
    }
    
    public Integer getLikeCount(Long postId) {
        return forumLikeMapper.countByPostId(postId);
    }
    
    public List<ForumComment> getCommentsByPostId(Long postId, Integer limit) {
        return forumPostMapper.selectCommentsByPostId(postId, limit != null ? limit : 10);
    }
    
    public List<ForumPost> getAllPosts() {
        return forumPostMapper.selectAllPostsWithCommentCount();
    }
    
    public List<ForumPost> getLikedPostsByUserId(Long userId) {
        return forumLikeMapper.selectLikedPostsByUserId(userId);
    }
    
    public void updatePostStatus(Long id, String status) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post != null) {
            post.setStatus(status);
            forumPostMapper.updateById(post);
        }
    }
    
    public void deletePost(Long id) {
        // 删除帖子相关的评论
        forumCommentMapper.deleteByPostId(id);
        // 删除帖子相关的点赞
        forumLikeMapper.deleteByPostId(id);
        // 删除帖子
        forumPostMapper.deleteById(id);
    }
    
    public void batchDeletePosts(List<Long> ids) {
        for (Long id : ids) {
            deletePost(id);
        }
    }
}
