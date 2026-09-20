package com.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("analysis_results")
public class AnalysisResults {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String stockCode;          // 股票代码
    private String stockName;           // 股票名称
    private Integer reportYear;         // 报告年份
    
    private BigDecimal revenue;         // 营业收入(亿元)
    private BigDecimal netProfit;      // 净利润(亿元)
    private BigDecimal deductNonRecurringProfit;  // 扣非净利润(亿元)
    private BigDecimal totalAssets;    // 总资产(亿元)
    private BigDecimal netAssets;      // 净资产(亿元)
    private BigDecimal eps;            // 每股收益
    private BigDecimal roe;            // 净资产收益率(%)
    private BigDecimal roa;            // 总资产收益率(%)
    private BigDecimal grossMargin;    // 毛利率(%)
    private BigDecimal netMargin;      // 净利率(%)
    private BigDecimal currentRatio;   // 流动比率
    private BigDecimal quickRatio;     // 速动比率
    private BigDecimal debtRatio;      // 资产负债率(%)
    private BigDecimal revenueGrowth;  // 营收增长率(%)
    private BigDecimal profitGrowth;   // 净利润增长率(%)
    
    private String aiAnalysis;         // AI分析报告
    private LocalDateTime analyzedAt; // 分析时间
}
