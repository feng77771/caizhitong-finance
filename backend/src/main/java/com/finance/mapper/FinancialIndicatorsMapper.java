package com.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.entity.FinancialIndicators;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface FinancialIndicatorsMapper extends BaseMapper<FinancialIndicators> {
    
    @Select("SELECT COUNT(DISTINCT stock_code) FROM financial_indicators")
    Long countDistinctStocks();
    
    @Select("SELECT COUNT(*) FROM financial_indicators")
    Long countTotalRecords();
    
    @Select("SELECT industry as name, COUNT(*) as value FROM financial_indicators GROUP BY industry ORDER BY value DESC")
    List<Map<String, Object>> getIndustryDistribution();

    @Select("SELECT report_date, stock_code, stock_name, eps, roe, gross_margin, revenue_growth, profit_growth, cash_flow, bps FROM financial_indicators WHERE stock_code = #{stockCode} ORDER BY report_date ASC")
    List<Map<String, Object>> getStockHistory(String stockCode);
    
    @Select("SELECT * FROM financial_indicators WHERE stock_code = #{stockCode} ORDER BY report_date DESC LIMIT 1")
    FinancialIndicators findByStockCode(String stockCode);
    
    @Select("SELECT * FROM financial_indicators WHERE stock_code = #{stockCode} AND report_date = #{reportDate}")
    FinancialIndicators findByStockCodeAndReportDate(String stockCode, Integer reportDate);
}