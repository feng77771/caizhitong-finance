package com.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("financial_indicators")
public class FinancialIndicators {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String stockCode;
    private String stockName;
    private String industry;
    private Integer reportDate;
    private BigDecimal eps;
    private BigDecimal roe;
    private BigDecimal grossMargin;
    private BigDecimal revenueGrowth;
    private BigDecimal profitGrowth;
    private BigDecimal cashFlow;
    private BigDecimal bps;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}