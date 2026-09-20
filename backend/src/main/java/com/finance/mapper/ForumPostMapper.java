package com.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.entity.ForumComment;
import com.finance.entity.ForumPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ForumPostMapper extends BaseMapper<ForumPost> {
    
    @Select("<script>" +
            "SELECT p.*, u.username, COALESCE(c.count, 0) as commentCount " +
            "FROM forum_post p " +
            "LEFT JOIN user u ON p.user_id = u.id " +
            "LEFT JOIN (SELECT post_id, COUNT(*) as count FROM forum_comment GROUP BY post_id) c ON p.id = c.post_id " +
            "WHERE p.status = 'normal' OR p.status = 'visible' " +
            "<if test=\"category != null and category != ''\">AND p.category = #{category}</if>" +
            "ORDER BY p.create_time DESC LIMIT #{limit}" +
            "</script>")
    List<ForumPost> selectLatestPosts(@Param("limit") Integer limit, @Param("category") String category);
    
    @Select("<script>" +
            "SELECT p.*, u.username, COALESCE(c.count, 0) as commentCount " +
            "FROM forum_post p " +
            "LEFT JOIN user u ON p.user_id = u.id " +
            "LEFT JOIN (SELECT post_id, COUNT(*) as count FROM forum_comment GROUP BY post_id) c ON p.id = c.post_id " +
            "WHERE p.status = 'normal' OR p.status = 'visible' " +
            "<if test=\"category != null and category != ''\">AND p.category = #{category}</if>" +
            "ORDER BY (p.likes + COALESCE(c.count, 0)) DESC LIMIT #{limit}" +
            "</script>")
    List<ForumPost> selectHotPosts(@Param("limit") Integer limit, @Param("category") String category);
    
    @Select("SELECT fc.*, u.username FROM forum_comment fc " +
            "LEFT JOIN user u ON fc.user_id = u.id " +
            "WHERE fc.post_id = #{postId} ORDER BY fc.create_time DESC LIMIT #{limit}")
    List<ForumComment> selectCommentsByPostId(@Param("postId") Long postId, @Param("limit") Integer limit);
    
    @Select("SELECT p.*, u.username, COALESCE(c.count, 0) as commentCount " +
            "FROM forum_post p " +
            "LEFT JOIN user u ON p.user_id = u.id " +
            "LEFT JOIN (SELECT post_id, COUNT(*) as count FROM forum_comment GROUP BY post_id) c ON p.id = c.post_id " +
            "WHERE p.category = #{category} ORDER BY p.create_time DESC")
    List<ForumPost> selectByCategory(@Param("category") String category);
    
    @Select("SELECT p.*, u.username, COALESCE(c.count, 0) as commentCount " +
            "FROM forum_post p " +
            "LEFT JOIN user u ON p.user_id = u.id " +
            "LEFT JOIN (SELECT post_id, COUNT(*) as count FROM forum_comment GROUP BY post_id) c ON p.id = c.post_id " +
            "ORDER BY p.create_time DESC")
    List<ForumPost> selectAllPostsWithCommentCount();
}
