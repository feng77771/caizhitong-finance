package com.finance.controller;

import com.finance.dto.ApiResponse;
import com.finance.service.FinancialIndicatorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/financial")
@CrossOrigin(origins = "*")
public class FinancialIndicatorsController {
    
    @Autowired
    private FinancialIndicatorsService financialIndicatorsService;
    
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statistics = financialIndicatorsService.getStatistics();
            return ApiResponse.success("获取统计数据成功", statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/industry-distribution")
    public ApiResponse<List<Map<String, Object>>> getIndustryDistribution() {
        try {
            List<Map<String, Object>> distribution = financialIndicatorsService.getIndustryDistribution();
            return ApiResponse.success("获取行业分布成功", distribution);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/stock-history/{stockCode}")
    public ApiResponse<List<Map<String, Object>>> getStockHistory(@PathVariable String stockCode) {
        try {
            List<Map<String, Object>> history = financialIndicatorsService.getStockHistory(stockCode);
            return ApiResponse.success("获取股票历史数据成功", history);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}