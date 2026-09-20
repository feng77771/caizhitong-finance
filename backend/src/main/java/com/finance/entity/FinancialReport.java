package com.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("financial_reports")
public class FinancialReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String stockCode;
    private String stockName;
    private Integer reportYear;
    private String reportType;
    private String pdfPath;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private LocalDateTime analyzedAt;  // 分析时间（非数据库字段）
}
