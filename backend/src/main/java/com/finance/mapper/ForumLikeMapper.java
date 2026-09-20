package com.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.entity.ForumLike;
import com.finance.entity.ForumPost;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ForumLikeMapper extends BaseMapper<ForumLike> {
    
    @Select("SELECT COUNT(*) FROM forum_like WHERE post_id = #{postId}")
    Integer countByPostId(@Param("postId") Long postId);
    
    @Select("SELECT COUNT(*) FROM forum_like WHERE post_id = #{postId} AND user_id = #{userId}")
    Integer countByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
    
    @Delete("DELETE FROM forum_like WHERE post_id = #{postId} AND user_id = #{userId}")
    void deleteByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
    
    @Delete("DELETE FROM forum_like WHERE post_id = #{postId}")
    void deleteByPostId(@Param("postId") Long postId);
    
    @Select("SELECT p.*, u.username FROM forum_post p JOIN forum_like l ON p.id = l.post_id JOIN user u ON p.user_id = u.id WHERE l.user_id = #{userId} ORDER BY l.create_time DESC")
    List<ForumPost> selectLikedPostsByUserId(@Param("userId") Long userId);
}
