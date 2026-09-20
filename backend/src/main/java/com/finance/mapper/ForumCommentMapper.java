package com.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.entity.ForumComment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ForumCommentMapper extends BaseMapper<ForumComment> {
    
    @Select("SELECT COUNT(*) FROM forum_comment WHERE post_id = #{postId}")
    Integer countByPostId(@Param("postId") Long postId);
    
    @Delete("DELETE FROM forum_comment WHERE post_id = #{postId}")
    void deleteByPostId(@Param("postId") Long postId);
}
