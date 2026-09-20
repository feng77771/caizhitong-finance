package com.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.entity.AnalysisResults;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AnalysisResultsMapper extends BaseMapper<AnalysisResults> {
    
    /**
     * 根据股票代码和报告年份查询分析结果
     */
    @Select("SELECT * FROM analysis_results WHERE stock_code = #{stockCode} AND report_year = #{reportYear}")
    AnalysisResults findByStockCodeAndReportYear(@Param("stockCode") String stockCode, @Param("reportYear") Integer reportYear);
}
