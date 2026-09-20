package com.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_post")
public class ForumPost {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String title;
    
    private String category;
    
    private String content;
    
    private Integer views;
    
    private Integer likes;
    
    private Integer isAnonymous;
    
    private String status;
    
    private LocalDateTime createTime;
    
    @TableField(exist = false)
    private String username;
    
    @TableField(exist = false)
    private Integer commentCount;
    
    @TableField(exist = false)
    private String displayName;
}
