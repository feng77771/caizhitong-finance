package com.finance.service;

import com.finance.mapper.FinancialIndicatorsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinancialIndicatorsService {
    
    @Autowired
    private FinancialIndicatorsMapper financialIndicatorsMapper;
    
    public Map<String, Object> getStatistics() {
        Long stockCount = financialIndicatorsMapper.countDistinctStocks();
        Long recordCount = financialIndicatorsMapper.countTotalRecords();
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("stockCount", stockCount);
        statistics.put("recordCount", recordCount);
        return statistics;
    }
    
    public List<Map<String, Object>> getIndustryDistribution() {
        return financialIndicatorsMapper.getIndustryDistribution();
    }

    public List<Map<String, Object>> getStockHistory(String stockCode) {
        return financialIndicatorsMapper.getStockHistory(stockCode);
    }
}