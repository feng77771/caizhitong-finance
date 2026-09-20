package com.finance.controller;

import com.finance.service.LlmAnalysisService;
import com.finance.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class AiController {

    private final LlmAnalysisService llmAnalysisService;

    public AiController(LlmAnalysisService llmAnalysisService) {
        this.llmAnalysisService = llmAnalysisService;
    }

    /**
     * 调用Ollama的Qwen2.5模型进行AI分析
     */
    @PostMapping("/analyze")
    public ApiResponse<String> analyze(@RequestBody Map<String, Object> request) {
        try {
            String prompt = (String) request.get("prompt");
            
            // 构建financialData，传入prompt
            Map<String, Object> financialData = new HashMap<>();
            financialData.put("prompt", prompt);
            
            // 调用Ollama的Qwen2.5模型
            String result = llmAnalysisService.analyzeFinancialReport("", financialData);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(500, "AI分析失败: " + e.getMessage());
        }
    }

    /**
     * 测试Ollama服务连接
     */
    @GetMapping("/test")
    public ApiResponse<String> testConnection() {
        try {
            Map<String, Object> financialData = new HashMap<>();
            financialData.put("prompt", "你好，请介绍一下自己。");
            
            String result = llmAnalysisService.analyzeFinancialReport("", financialData);
            
            return ApiResponse.success("Ollama服务连接成功！\n" + result);
        } catch (Exception e) {
            return ApiResponse.error(500, "Ollama服务连接失败: " + e.getMessage());
        }
    }
}
