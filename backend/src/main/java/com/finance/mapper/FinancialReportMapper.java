package com.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.entity.FinancialReport;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface FinancialReportMapper extends BaseMapper<FinancialReport> {
    
    @Select("SELECT * FROM financial_reports WHERE stock_code = #{stockCode} ORDER BY report_year DESC")
    List<FinancialReport> findByStockCode(String stockCode);
    
    @Select("SELECT * FROM financial_reports WHERE stock_code = #{stockCode} AND report_year = #{year}")
    FinancialReport findByStockCodeAndYear(@Param("stockCode") String stockCode, @Param("year") Integer year);
    
    @Select("SELECT * FROM financial_reports ORDER BY create_time DESC")
    List<FinancialReport> findAllOrderByCreateTime();
    
    @Insert("INSERT INTO financial_reports (stock_code, stock_name, report_year, report_type, pdf_path) VALUES (#{stockCode}, #{stockName}, #{reportYear}, #{reportType}, #{pdfPath})")
    int insert(FinancialReport report);
    
    @Delete("DELETE FROM financial_reports WHERE id = #{id}")
    int deleteById(Long id);
}
