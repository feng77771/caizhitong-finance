package com.finance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class LlmAnalysisService {

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Ollama本地部署的Qwen模型
    @Value("${llm.api.url:http://localhost:11434/api/generate}")
    private String llmApiUrl;

    @Value("${llm.api.model:qwen2.5:3b}")
    private String llmModel;

    @Value("${llm.api.key:}")
    private String llmApiKey;

    /**
     * 分析财报数据，生成AI分析报告
     */
    public String analyzeFinancialReport(String reportText, Map<String, Object> financialData) {
        String prompt;
        
        // 如果是对话请求，直接使用传入的prompt
        if (financialData.containsKey("prompt")) {
            prompt = (String) financialData.get("prompt");
        } else {
            prompt = buildAnalysisPrompt(reportText, financialData);
        }
        
        try {
            System.out.println("========== 开始调用LLM API ==========");
            System.out.println("API地址: " + llmApiUrl);
            System.out.println("模型: " + llmModel);
            System.out.println("使用Ollama本地部署");
            String result = callLlmApi(prompt);
            System.out.println("========== LLM API调用成功 ==========");
            System.out.println("响应长度: " + result.length() + " 字符");
            System.out.println("响应摘要: " + (result.length() > 100 ? result.substring(0, 100) + "..." : result));
            return result;
        } catch (IOException e) {
            System.err.println("========== LLM API调用失败 ==========");
            System.err.println("IO错误: " + e.getMessage());
            e.printStackTrace();
            System.err.println("将使用模拟分析报告");
            return generateMockAnalysis(financialData);
        } catch (Exception e) {
            System.err.println("========== LLM API调用失败 ==========");
            System.err.println("错误类型: " + e.getClass().getName());
            System.err.println("错误信息: " + e.getMessage());
            e.printStackTrace();
            System.err.println("将使用模拟分析报告");
            return generateMockAnalysis(financialData);
        }
    }

    /**
     * 构建分析提示词
     */
    private String buildAnalysisPrompt(String reportText, Map<String, Object> financialData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请作为一名资深财务分析师，对以下财报数据进行深度分析：\n\n");
        
        prompt.append("【财务指标数据】\n");
        prompt.append("盈利能力分析：\n");
        prompt.append("  - 净资产收益率(ROE): ").append(financialData.get("roe")).append("\n");
        prompt.append("  - 总资产收益率(ROA): ").append(financialData.get("roa")).append("\n");
        prompt.append("  - 毛利率: ").append(financialData.get("grossMargin")).append("\n");
        prompt.append("  - 净利率: ").append(financialData.get("netMargin")).append("\n\n");
        
        prompt.append("偿债能力分析：\n");
        prompt.append("  - 流动比率: ").append(financialData.get("currentRatio")).append("\n");
        prompt.append("  - 速动比率: ").append(financialData.get("quickRatio")).append("\n");
        prompt.append("  - 资产负债率: ").append(financialData.get("debtRatio")).append("\n\n");
        
        prompt.append("营运能力分析：\n");
        prompt.append("  - 存货周转率: ").append(financialData.get("inventoryTurnover")).append("\n");
        prompt.append("  - 应收账款周转率: ").append(financialData.get("receivableTurnover")).append("\n");
        prompt.append("  - 总资产周转率: ").append(financialData.get("assetTurnover")).append("\n\n");
        
        prompt.append("成长能力分析：\n");
        prompt.append("  - 营业收入增长率: ").append(financialData.get("revenueGrowth")).append("\n");
        prompt.append("  - 净利润增长率: ").append(financialData.get("profitGrowth")).append("\n\n");
        
        if (reportText != null && !reportText.isEmpty()) {
            prompt.append("【财报原文摘要】\n");
            prompt.append(reportText.substring(0, Math.min(reportText.length(), 2000))).append("\n\n");
        }
        
        prompt.append("请按照以下结构输出分析报告：\n");
        prompt.append("1. 公司概况（公司名称、股票代码、报告年份）\n");
        prompt.append("2. 财务亮点（从各项指标分析优势）\n");
        prompt.append("3. 风险提示（潜在风险和注意事项）\n");
        prompt.append("4. 综合评价（投资建议和展望）\n");
        prompt.append("\n请使用专业但易懂的语言，输出格式为HTML格式，包含标题和列表。");
        
        return prompt.toString();
    }

    /**
     * 调用LLM API（Ollama本地部署的Qwen模型）
     */
    private String callLlmApi(String prompt) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", llmModel);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        
        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.7);
        options.put("num_predict", 4096);
        requestBody.put("options", options);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(llmApiUrl)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("LLM API请求失败: " + response);
            }
            
            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);
            return root.get("response").asText();
        }
    }

    /**
     * 生成模拟分析报告（当LLM服务不可用时）
     */
    private String generateMockAnalysis(Map<String, Object> financialData) {
        String stockName = (String) financialData.getOrDefault("stockName", "该公司");
        String reportYear = (String) financialData.getOrDefault("reportYear", "");
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("<p><strong>").append(stockName).append(reportYear).append("年度报告分析</strong></p>\n");
        analysis.append("<p>根据财报数据，公司整体经营状况良好，各项财务指标表现稳健。</p>\n");
        
        analysis.append("<p><strong>财务亮点：</strong></p>\n");
        analysis.append("<ul>\n");
        String roe = (String) financialData.get("roe");
        if (!roe.equals("-")) {
            analysis.append("<li>净资产收益率 ").append(roe).append("，显示较强的股东回报能力</li>\n");
        }
        String grossMargin = (String) financialData.get("grossMargin");
        if (!grossMargin.equals("-")) {
            analysis.append("<li>毛利率 ").append(grossMargin).append("，产品盈利能力较强</li>\n");
        }
        String revenueGrowth = (String) financialData.get("revenueGrowth");
        if (!revenueGrowth.equals("-")) {
            boolean isPositive = !revenueGrowth.startsWith("-");
            analysis.append("<li>营收增长率 ").append(revenueGrowth).append("，").append(isPositive ? "保持增长态势" : "面临增长压力").append("</li>\n");
        }
        analysis.append("</ul>\n");
        
        analysis.append("<p><strong>风险提示：</strong></p>\n");
        analysis.append("<ul>\n");
        analysis.append("<li>宏观经济环境变化可能影响公司业绩</li>\n");
        analysis.append("<li>行业竞争加剧，需关注市场份额变化</li>\n");
        analysis.append("<li>原材料价格波动可能影响毛利率</li>\n");
        analysis.append("</ul>\n");
        
        analysis.append("<p><strong>综合评价：</strong>").append(stockName).append("财务结构稳健，具备一定的投资价值，建议持续关注公司后续发展动态。</p>");
        
        return analysis.toString();
    }

    /**
     * 生成财务指标摘要
     */
    public Map<String, Object> extractMetrics(String reportText) {
        Map<String, Object> metrics = new HashMap<>();
        
        // 提取关键指标
        metrics.put("roe", extractValue(reportText, "净资产收益率|ROE"));
        metrics.put("roa", extractValue(reportText, "总资产收益率|ROA"));
        metrics.put("grossMargin", extractValue(reportText, "毛利率"));
        metrics.put("netMargin", extractValue(reportText, "净利率"));
        metrics.put("currentRatio", extractValue(reportText, "流动比率"));
        metrics.put("quickRatio", extractValue(reportText, "速动比率"));
        metrics.put("debtRatio", extractValue(reportText, "资产负债率"));
        metrics.put("inventoryTurnover", extractValue(reportText, "存货周转率"));
        metrics.put("receivableTurnover", extractValue(reportText, "应收账款周转率"));
        metrics.put("assetTurnover", extractValue(reportText, "总资产周转率"));
        metrics.put("revenueGrowth", extractValue(reportText, "营业收入增长率|营收增长率"));
        metrics.put("profitGrowth", extractValue(reportText, "净利润增长率"));
        
        return metrics;
    }

    private String extractValue(String text, String keyword) {
        if (text == null || text.isEmpty()) {
            return "-";
        }
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            keyword + "[\\s\\S]*?([\\-+]?[\\d.]+%?)"
        );
        java.util.regex.Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : "-";
    }
}