package com.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("bar_manager_application")
public class BarManagerApplication {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    @TableField(exist = false)
    private String username;
    private String category;
    private String reason;
    private String experience;
    private String status;
    private Long reviewUserId;
    private String reviewComment;
    private LocalDateTime createTime;
    private LocalDateTime reviewTime;
}
