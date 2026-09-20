package com.finance.controller;

import com.finance.dto.ApiResponse;
import com.finance.entity.FinancialIndicators;
import com.finance.mapper.FinancialIndicatorsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/risk")
public class RiskController {

    @Autowired
    private FinancialIndicatorsMapper financialIndicatorsMapper;

    @Autowired
    private RestTemplate restTemplate;

    // 2025年最新报告日期
    private static final Integer LATEST_REPORT_DATE = 20251231;

    @GetMapping("/stock-info/{stockCode}")
    public ApiResponse<Map<String, Object>> getStockInfo(@PathVariable String stockCode) {
        // 使用2025年最新数据
        FinancialIndicators fi = financialIndicatorsMapper.findByStockCodeAndReportDate(stockCode, LATEST_REPORT_DATE);
        
        if (fi == null) {
            // 如果2025年数据不存在，尝试获取最新数据
            fi = financialIndicatorsMapper.findByStockCode(stockCode);
            if (fi == null) {
                return ApiResponse.success("未找到股票信息", null);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("stockCode", stockCode);
        result.put("name", fi.getStockName());
        
        return ApiResponse.success("获取股票信息成功", result);
    }

    @GetMapping("/{stockCode}")
    public ApiResponse<Map<String, Object>> getRiskAssessment(@PathVariable String stockCode) {
        // 使用2025年最新数据进行风险评估
        FinancialIndicators fi = financialIndicatorsMapper.findByStockCodeAndReportDate(stockCode, LATEST_REPORT_DATE);
        
        if (fi == null) {
            // 如果2025年数据不存在，尝试获取最新数据
            fi = financialIndicatorsMapper.findByStockCode(stockCode);
            if (fi == null) {
                return ApiResponse.error("未找到该股票的财务数据");
            }
        }

        // 构建请求参数（使用实际财务指标数据）
        Map<String, Object> req = new HashMap<>();
        req.put("EPS", fi.getEps() != null ? fi.getEps().doubleValue() : 0.0);
        req.put("ROE", fi.getRoe() != null ? fi.getRoe().doubleValue() : 0.0);
        req.put("gross_margin", fi.getGrossMargin() != null ? fi.getGrossMargin().doubleValue() : 0.0);
        req.put("revenue_growth", fi.getRevenueGrowth() != null ? fi.getRevenueGrowth().doubleValue() : 0.0);
        req.put("profit_growth", fi.getProfitGrowth() != null ? fi.getProfitGrowth().doubleValue() : 0.0);
        req.put("cash_flow", fi.getCashFlow() != null ? fi.getCashFlow().doubleValue() : 0.0);
        req.put("bps", fi.getBps() != null ? fi.getBps().doubleValue() : 0.0);

        try {
            // 调用Flask预测服务（以XGBoost模型预测结果为准，不使用数据库中的risk字段）
            Map result = restTemplate.postForObject(
                    "http://127.0.0.1:5005/predict",
                    req,
                    Map.class
            );

            if (result == null) {
                return ApiResponse.error("风险评估服务返回为空");
            }

            // 添加公司名称和股票代码
            result.put("companyName", fi.getStockName());
            result.put("stockCode", stockCode);

            return ApiResponse.success("风险评估成功", result);
        } catch (Exception e) {
            // 如果Flask服务调用失败，使用本地计算
            return calculateRiskLocally(fi, stockCode);
        }
    }

    private ApiResponse<Map<String, Object>> calculateRiskLocally(FinancialIndicators fi, String stockCode) {
        Map<String, Object> result = new HashMap<>();
        
        double eps = fi.getEps() != null ? fi.getEps().doubleValue() : 0.0;
        double roe = fi.getRoe() != null ? fi.getRoe().doubleValue() : 0.0;
        double grossMargin = fi.getGrossMargin() != null ? fi.getGrossMargin().doubleValue() : 0.0;
        double revenueGrowth = fi.getRevenueGrowth() != null ? fi.getRevenueGrowth().doubleValue() : 0.0;
        double profitGrowth = fi.getProfitGrowth() != null ? fi.getProfitGrowth().doubleValue() : 0.0;
        double cashFlow = fi.getCashFlow() != null ? fi.getCashFlow().doubleValue() : 0.0;
        double bps = fi.getBps() != null ? fi.getBps().doubleValue() : 0.0;

        // 计算各指标的风险评分
        int roeScore = calculateRoeScore(roe);
        int epsScore = calculateEpsScore(eps);
        int grossScore = calculateGrossScore(grossMargin);
        int revenueScore = calculateRevenueScore(revenueGrowth);
        int profitScore = calculateProfitScore(profitGrowth);
        int cashScore = calculateCashScore(cashFlow);
        int bpsScore = calculateBpsScore(bps);

        // 综合风险得分（加权计算，结果0-100分）
        double riskProb = roeScore * 0.25 + profitScore * 0.20 + cashScore * 0.20 + 
                          revenueScore * 0.15 + grossScore * 0.10 + epsScore * 0.05 + bpsScore * 0.05;
        riskProb = Math.round(riskProb * 100.0) / 100.0;

        // 根据风险概率确定风险等级
        String riskLevel;
        if (riskProb < 30) {
            riskLevel = "低风险";
        } else if (riskProb < 60) {
            riskLevel = "中风险";
        } else {
            riskLevel = "高风险";
        }

        Map<String, Object> indicators = new HashMap<>();
        indicators.put("roe", createIndicatorMap(roeScore, 25, Math.round(roe * 100.0) / 100.0));
        indicators.put("eps", createIndicatorMap(epsScore, 5, Math.round(eps * 10000.0) / 10000.0));
        indicators.put("gross_margin", createIndicatorMap(grossScore, 10, Math.round(grossMargin * 100.0) / 100.0));
        indicators.put("revenue_growth", createIndicatorMap(revenueScore, 15, Math.round(revenueGrowth * 100.0) / 100.0));
        indicators.put("profit_growth", createIndicatorMap(profitScore, 20, Math.round(profitGrowth * 100.0) / 100.0));
        indicators.put("cash_flow", createIndicatorMap(cashScore, 20, Math.round(cashFlow * 1000000.0) / 1000000.0));
        indicators.put("bps", createIndicatorMap(bpsScore, 5, Math.round(bps * 100.0) / 100.0));

        result.put("riskProb", riskProb);
        result.put("riskLevel", riskLevel);
        result.put("indicators", indicators);
        result.put("companyName", fi.getStockName());
        result.put("stockCode", stockCode);

        return ApiResponse.success("风险评估成功（本地计算）", result);
    }

    private int calculateRoeScore(double roe) {
        if (roe > 15) return 0;
        if (roe > 10) return 15;
        if (roe > 5) return 30;
        if (roe > 0) return 60;
        return 100;
    }

    private int calculateEpsScore(double eps) {
        if (eps > 1) return 0;
        if (eps > 0.5) return 25;
        if (eps > 0) return 50;
        return 100;
    }

    private int calculateGrossScore(double grossMargin) {
        if (grossMargin > 30) return 0;
        if (grossMargin > 20) return 20;
        if (grossMargin > 10) return 50;
        if (grossMargin > 0) return 75;
        return 100;
    }

    private int calculateRevenueScore(double revenueGrowth) {
        if (revenueGrowth > 20) return 0;
        if (revenueGrowth > 10) return 20;
        if (revenueGrowth > 0) return 40;
        if (revenueGrowth > -10) return 70;
        return 100;
    }

    private int calculateProfitScore(double profitGrowth) {
        if (profitGrowth > 20) return 0;
        if (profitGrowth > 10) return 20;
        if (profitGrowth > 0) return 40;
        if (profitGrowth > -20) return 70;
        return 100;
    }

    private int calculateCashScore(double cashFlow) {
        return cashFlow > 0 ? 0 : 70;
    }

    private int calculateBpsScore(double bps) {
        if (bps > 5) return 0;
        if (bps > 3) return 25;
        if (bps > 1) return 50;
        return 100;
    }
    
    private Map<String, Object> createIndicatorMap(int score, int weight, double value) {
        Map<String, Object> map = new HashMap<>();
        map.put("score", score);
        map.put("weight", weight);
        map.put("value", value);
        return map;
    }
}