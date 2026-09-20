package com.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("bar_manager")
public class BarManager {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String category;
    private LocalDateTime startTime;
    private String status;
    private String username;
}
