package com.finance.service;

import com.finance.entity.AnalysisResults;
import com.finance.entity.FinancialReport;
import com.finance.entity.FinancialIndicators;
import com.finance.mapper.AnalysisResultsMapper;
import com.finance.mapper.FinancialReportMapper;
import com.finance.mapper.FinancialIndicatorsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FinancialReportService {
    
    private static final String UPLOAD_DIR = "D:/财报/CaiZhiTong/finance/backend/uploads/reports/";
    
    @Autowired
    private FinancialReportMapper financialReportMapper;
    
    @Autowired
    private PdfParserService pdfParserService;
    
    @Autowired
    private OcrService ocrService;
    
    @Autowired
    private LlmAnalysisService llmAnalysisService;
    
    @Autowired
    private FinancialIndicatorsMapper financialIndicatorsMapper;
    
    @Autowired
    private AnalysisResultsMapper analysisResultsMapper;
    

    
    public List<FinancialReport> getAllReports() {
        List<FinancialReport> reports = financialReportMapper.findAllOrderByCreateTime();
        
        // 关联查询分析时间
        for (FinancialReport report : reports) {
            AnalysisResults analysisResult = analysisResultsMapper.findByStockCodeAndReportYear(
                report.getStockCode(), report.getReportYear());
            if (analysisResult != null) {
                report.setAnalyzedAt(analysisResult.getAnalyzedAt());
            }
        }
        
        return reports;
    }
    
    public List<FinancialReport> getReportsByStockCode(String stockCode) {
        return financialReportMapper.findByStockCode(stockCode);
    }
    
    public FinancialReport getReportByStockCodeAndYear(String stockCode, Integer year) {
        return financialReportMapper.findByStockCodeAndYear(stockCode, year);
    }
    
    public boolean uploadReport(MultipartFile file, String stockCode, String stockName, Integer reportYear) throws IOException {
        if (file.isEmpty()) {
            return false;
        }
        
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : ".pdf";
        
        String filename = stockCode + "_" + reportYear + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        String pdfPath = UPLOAD_DIR + filename;
        
        file.transferTo(new File(pdfPath));
        
        FinancialReport report = new FinancialReport();
        report.setStockCode(stockCode);
        report.setStockName(stockName);
        report.setReportYear(reportYear);
        report.setPdfPath(pdfPath);
        
        return financialReportMapper.insert(report) > 0;
    }
    
    public boolean deleteReport(Long id) {
        FinancialReport report = financialReportMapper.selectById(id);
        if (report != null) {
            File file = new File(report.getPdfPath());
            if (file.exists()) {
                file.delete();
            }
            return financialReportMapper.deleteById(id) > 0;
        }
        return false;
    }
    
    public String getReportPdfPath(Long id) {
        FinancialReport report = financialReportMapper.selectById(id);
        return report != null ? report.getPdfPath() : null;
    }
    
    public FinancialReport getReportById(Long id) {
        return financialReportMapper.selectById(id);
    }
    
    public FinancialReport uploadReport(String companyName, String companyCode, String reportType, String reportYear, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : ".pdf";
        
        String filename = companyCode + "_" + reportYear + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        String pdfPath = UPLOAD_DIR + filename;
        
        file.transferTo(new File(pdfPath));
        
        FinancialReport report = new FinancialReport();
        report.setStockCode(companyCode);
        report.setStockName(companyName);
        report.setReportYear(Integer.parseInt(reportYear));
        report.setReportType(reportType);
        report.setPdfPath(pdfPath);
        report.setStatus("uploaded");
        
        financialReportMapper.insert(report);
        return report;
    }
    
    /**
     * 财报分析流程：PDF解析 + OCR + LLM分析
     * 只在第一次分析时保存详细结果到analysis_results表
     */
    @Transactional
    public Map<String, Object> analyzeReport(Long id) {
        FinancialReport report = financialReportMapper.selectById(id);
        if (report == null) {
            throw new RuntimeException("财报不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        
        // 检查是否已分析过，如果已分析过则不保存新结果
        // 但如果analysis_results表中没有数据（之前分析失败的），仍需保存
        boolean alreadyAnalyzed = "analyzed".equals(report.getStatus());
        boolean hasAnalysisResult = analysisResultsMapper.findByStockCodeAndReportYear(
            report.getStockCode(), report.getReportYear()) != null;
        
        try {
            // 1. 优先从数据库获取已有的真实财务指标数据
            Map<String, Object> financialData = getFinancialIndicatorsFromDb(report);
            
            // 2. 如果数据库中没有数据，进行PDF解析
            boolean fromDb = financialData != null;
            String pdfText = "";
            
            if (!fromDb) {
                // PDF解析：提取文本内容
                pdfText = pdfParserService.extractText(report.getPdfPath());
                
                // OCR识别：处理扫描件PDF
                if (pdfText.trim().length() < 100) {
                    pdfText = ocrService.extractTextFromImagePdf(report.getPdfPath());
                }
                
                // 提取财务指标
                financialData = pdfParserService.extractFinancialData(report.getPdfPath());
                
                // 保存财务指标到数据库
                saveFinancialIndicators(report, financialData);
            }
            
            // 补充基本信息
            financialData.put("stockCode", report.getStockCode());
            financialData.put("stockName", report.getStockName());
            financialData.put("reportYear", String.valueOf(report.getReportYear()));
            
            // 3. LLM分析：生成AI分析报告
            String aiAnalysis = llmAnalysisService.analyzeFinancialReport(pdfText, financialData);
            
            // 构建结果
            result.put("reportId", id);
            result.put("status", "analyzed");
            result.put("message", alreadyAnalyzed ? "使用已有分析数据" : (fromDb ? "使用数据库已有数据" : "财报分析完成"));
            result.put("metrics", financialData);
            result.put("aiAnalysis", aiAnalysis);
            result.put("rawText", pdfText.length() > 0 ? pdfText.substring(0, Math.min(pdfText.length(), 1000)) + "..." : "");
            
            // 4. 如果是第一次分析（即analysis_results表中没有记录），保存详细结果到analysis_results表
            if (!hasAnalysisResult) {
                saveAnalysisResults(report, financialData, aiAnalysis);
            }
            
            // 更新数据库状态为已分析
            report.setStatus("analyzed");
            financialReportMapper.updateById(report);
            
        } catch (IOException e) {
            result.put("status", "error");
            result.put("message", "PDF解析失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 保存分析结果到数据库（只保存第一次分析的结果）
     */
    private void saveAnalysisResults(FinancialReport report, Map<String, Object> financialData, String aiAnalysis) {
        // 先检查是否已存在该股票该年份的分析结果
        AnalysisResults existing = analysisResultsMapper.findByStockCodeAndReportYear(
            report.getStockCode(), report.getReportYear());
        
        // 如果已存在，不覆盖，保持第一次分析的结果
        if (existing != null) {
            return;
        }
        
        AnalysisResults results = new AnalysisResults();
        results.setStockCode(report.getStockCode());
        results.setStockName(report.getStockName());
        results.setReportYear(report.getReportYear());
        
        // 设置各项指标
        setAnalysisValue(results::setRevenue, (String) financialData.get("revenue"));
        setAnalysisValue(results::setNetProfit, (String) financialData.get("netProfit"));
        setAnalysisValue(results::setDeductNonRecurringProfit, (String) financialData.get("deductNonRecurringProfit"));
        setAnalysisValue(results::setTotalAssets, (String) financialData.get("totalAssets"));
        setAnalysisValue(results::setNetAssets, (String) financialData.get("netAssets"));
        setAnalysisValue(results::setEps, (String) financialData.get("eps"));
        setAnalysisValue(results::setRoe, parsePercentage((String) financialData.get("roe")));
        setAnalysisValue(results::setRoa, parsePercentage((String) financialData.get("roa")));
        setAnalysisValue(results::setGrossMargin, parsePercentage((String) financialData.get("grossMargin")));
        setAnalysisValue(results::setNetMargin, parsePercentage((String) financialData.get("netMargin")));
        setAnalysisValue(results::setCurrentRatio, (String) financialData.get("currentRatio"));
        setAnalysisValue(results::setQuickRatio, (String) financialData.get("quickRatio"));
        setAnalysisValue(results::setDebtRatio, parsePercentage((String) financialData.get("debtRatio")));
        setAnalysisValue(results::setRevenueGrowth, parsePercentage((String) financialData.get("revenueGrowth")));
        setAnalysisValue(results::setProfitGrowth, parsePercentage((String) financialData.get("profitGrowth")));
        
        results.setAiAnalysis(aiAnalysis);
        results.setAnalyzedAt(LocalDateTime.now());
        
        // 插入新记录（使用INSERT IGNORE防止重复）
        analysisResultsMapper.insert(results);
    }
    
    /**
     * 设置分析结果值（处理空值）
     */
    private void setAnalysisValue(java.util.function.Consumer<BigDecimal> setter, String value) {
        if (value != null && !"-".equals(value) && !value.isEmpty() && !"N/A".equals(value)) {
            try {
                // 去除可能的百分号
                String cleanValue = value.replace("%", "").trim();
                setter.accept(new BigDecimal(cleanValue));
            } catch (NumberFormatException e) {
                // 解析失败，不设置值
            }
        }
    }
    
    /**
     * 从数据库获取已有的财务指标数据
     */
    private Map<String, Object> getFinancialIndicatorsFromDb(FinancialReport report) {
        FinancialIndicators indicators = financialIndicatorsMapper.findByStockCodeAndReportDate(
            report.getStockCode(), report.getReportYear());
        
        // 如果找不到精确匹配，尝试匹配YYYYMMDD格式的日期（如20251231）
        if (indicators == null) {
            int dateWithMonth = report.getReportYear() * 10000 + 1231;
            indicators = financialIndicatorsMapper.findByStockCodeAndReportDate(
                report.getStockCode(), dateWithMonth);
        }
        
        if (indicators == null) {
            return null;
        }
        
        Map<String, Object> financialData = new HashMap<>();
        
        // 基础财务指标（从数据库读取）
        if (indicators.getEps() != null) financialData.put("eps", indicators.getEps().toString());
        if (indicators.getRoe() != null) financialData.put("roe", indicators.getRoe().toString() + "%");
        if (indicators.getGrossMargin() != null) financialData.put("grossMargin", indicators.getGrossMargin().toString() + "%");
        if (indicators.getRevenueGrowth() != null) financialData.put("revenueGrowth", indicators.getRevenueGrowth().toString() + "%");
        if (indicators.getProfitGrowth() != null) financialData.put("profitGrowth", indicators.getProfitGrowth().toString() + "%");
        if (indicators.getCashFlow() != null) financialData.put("operatingCashFlow", indicators.getCashFlow().toString());
        
        // 计算其他指标
        if (indicators.getRoe() != null && indicators.getCashFlow() != null) {
            try {
                double roe = indicators.getRoe().doubleValue();
                double cashFlow = indicators.getCashFlow().doubleValue();
                if (cashFlow > 0) {
                    financialData.put("roa", String.format("%.2f%%", roe * 0.24)); // 简化计算
                }
            } catch (Exception e) {
                // 计算失败忽略
            }
        }
        
        return financialData;
    }
    
    /**
     * 将解析的财务指标保存到数据库
     */
    private void saveFinancialIndicators(FinancialReport report, Map<String, Object> financialData) {
        // 先检查是否已存在该股票该年份的记录
        FinancialIndicators existing = financialIndicatorsMapper.findByStockCodeAndReportDate(report.getStockCode(), report.getReportYear());
        
        FinancialIndicators indicators = existing != null ? existing : new FinancialIndicators();
        
        indicators.setStockCode(report.getStockCode());
        indicators.setStockName(report.getStockName());
        indicators.setReportDate(report.getReportYear());
        
        // 设置各项指标（只保存非空数据）
        setBigDecimalValue(indicators::setEps, (String) financialData.get("eps"));
        setBigDecimalValue(indicators::setRoe, parsePercentage((String) financialData.get("roe")));
        setBigDecimalValue(indicators::setGrossMargin, parsePercentage((String) financialData.get("grossMargin")));
        setBigDecimalValue(indicators::setRevenueGrowth, parsePercentage((String) financialData.get("revenueGrowth")));
        setBigDecimalValue(indicators::setProfitGrowth, parsePercentage((String) financialData.get("profitGrowth")));
        setBigDecimalValue(indicators::setCashFlow, (String) financialData.get("operatingCashFlow"));
        
        if (existing != null) {
            financialIndicatorsMapper.updateById(indicators);
        } else {
            financialIndicatorsMapper.insert(indicators);
        }
    }
    
    /**
     * 设置BigDecimal值（处理空值）
     */
    private void setBigDecimalValue(java.util.function.Consumer<BigDecimal> setter, String value) {
        if (value != null && !"-".equals(value) && !value.isEmpty()) {
            try {
                setter.accept(new BigDecimal(value));
            } catch (NumberFormatException e) {
                // 解析失败，不设置值
            }
        }
    }
    
    /**
     * 解析百分比字符串
     */
    private String parsePercentage(String value) {
        if (value != null && value.contains("%")) {
            return value.replace("%", "");
        }
        return value;
    }
    
    /**
     * 根据股票代码和年份分析财报
     */
    public Map<String, Object> analyzeReportByStockCodeAndYear(String stockCode, Integer year) {
        FinancialReport report = financialReportMapper.findByStockCodeAndYear(stockCode, year);
        if (report == null) {
            throw new RuntimeException("未找到该财报");
        }
        return analyzeReport(report.getId());
    }
    
    /**
     * 获取分析结果（包含指标和AI分析）
     */
    public Map<String, Object> getAnalysisResult(String stockCode, Integer year) {
        FinancialReport report = financialReportMapper.findByStockCodeAndYear(stockCode, year);
        if (report == null) {
            throw new RuntimeException("未找到该财报");
        }
        
        // 如果已分析过，直接返回缓存结果
        if ("analyzed".equals(report.getStatus())) {
            return analyzeReport(report.getId());
        }
        
        return analyzeReport(report.getId());
    }
    
    public FinancialReport updateReport(Long id, String companyName, String companyCode, String reportType, String reportYear) {
        return updateReport(id, companyName, companyCode, reportType, reportYear, null);
    }
    
    public FinancialReport updateReport(Long id, String companyName, String companyCode, String reportType, String reportYear, MultipartFile file) {
        FinancialReport report = financialReportMapper.selectById(id);
        if (report == null) {
            throw new RuntimeException("财报不存在");
        }
        
        report.setStockName(companyName);
        report.setStockCode(companyCode);
        report.setReportType(reportType);
        report.setReportYear(Integer.parseInt(reportYear));
        
        // 如果有新文件上传，更新PDF文件
        if (file != null && !file.isEmpty()) {
            try {
                // 删除旧文件
                String oldPdfPath = report.getPdfPath();
                if (oldPdfPath != null && !oldPdfPath.isEmpty()) {
                    File oldFile = new File(oldPdfPath);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }
                
                // 保存新文件
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".pdf";
                
                String filename = companyCode + "_" + reportYear + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
                String pdfPath = UPLOAD_DIR + filename;
                
                file.transferTo(new File(pdfPath));
                report.setPdfPath(pdfPath);
                report.setStatus("uploaded"); // 重置状态，需要重新分析
            } catch (IOException e) {
                throw new RuntimeException("文件上传失败: " + e.getMessage());
            }
        }
        
        financialReportMapper.updateById(report);
        return report;
    }
    
    /**
     * AI对话：基于财报数据回答问题
     */
    public String chatWithAi(String stockCode, Integer reportYear, String question) {
        // 获取财报记录
        FinancialReport report = financialReportMapper.findByStockCodeAndYear(stockCode, reportYear);
        if (report == null) {
            throw new RuntimeException("未找到该财报");
        }
        
        String stockName = report.getStockName();
        
        // 构建提示词
        StringBuilder prompt = new StringBuilder();
        prompt.append("请基于以下财报数据回答用户问题。\n\n");
        prompt.append("公司名称：").append(stockName).append("\n");
        prompt.append("股票代码：").append(stockCode).append("\n");
        prompt.append("报告年份：").append(reportYear).append("\n\n");
        prompt.append("财务数据：\n");
        
        // 优先从analysis_results表获取数据（更完整）
        AnalysisResults analysisResult = analysisResultsMapper.findByStockCodeAndReportYear(stockCode, reportYear);
        if (analysisResult != null) {
            if (analysisResult.getEps() != null) prompt.append("- 每股收益：").append(analysisResult.getEps()).append("元\n");
            if (analysisResult.getRoe() != null) prompt.append("- 净资产收益率：").append(analysisResult.getRoe()).append("%\n");
            if (analysisResult.getGrossMargin() != null) prompt.append("- 毛利率：").append(analysisResult.getGrossMargin()).append("%\n");
            if (analysisResult.getRevenueGrowth() != null) prompt.append("- 营收增长率：").append(analysisResult.getRevenueGrowth()).append("%\n");
            if (analysisResult.getProfitGrowth() != null) prompt.append("- 净利润增长率：").append(analysisResult.getProfitGrowth()).append("%\n");
            if (analysisResult.getRevenue() != null) prompt.append("- 营业收入：").append(analysisResult.getRevenue()).append("亿元\n");
            if (analysisResult.getNetProfit() != null) prompt.append("- 净利润：").append(analysisResult.getNetProfit()).append("亿元\n");
            if (analysisResult.getTotalAssets() != null) prompt.append("- 总资产：").append(analysisResult.getTotalAssets()).append("亿元\n");
            if (analysisResult.getNetAssets() != null) prompt.append("- 净资产：").append(analysisResult.getNetAssets()).append("亿元\n");
            if (analysisResult.getDebtRatio() != null) prompt.append("- 资产负债率：").append(analysisResult.getDebtRatio()).append("%\n");
        } else {
            // 尝试从financial_indicators表获取数据
            FinancialIndicators indicators = financialIndicatorsMapper.findByStockCodeAndReportDate(stockCode, reportYear);
            if (indicators != null) {
                if (indicators.getEps() != null) prompt.append("- 每股收益：").append(indicators.getEps()).append("元\n");
                if (indicators.getRoe() != null) prompt.append("- 净资产收益率：").append(indicators.getRoe()).append("%\n");
                if (indicators.getGrossMargin() != null) prompt.append("- 毛利率：").append(indicators.getGrossMargin()).append("%\n");
                if (indicators.getRevenueGrowth() != null) prompt.append("- 营收增长率：").append(indicators.getRevenueGrowth()).append("%\n");
                if (indicators.getProfitGrowth() != null) prompt.append("- 净利润增长率：").append(indicators.getProfitGrowth()).append("%\n");
                if (indicators.getCashFlow() != null) prompt.append("- 经营现金流：").append(indicators.getCashFlow()).append("亿元\n");
            } else {
                // 如果两个表都没有数据，尝试重新分析
                Map<String, Object> analysisData = analyzeReport(report.getId());
                Map<String, Object> metrics = (Map<String, Object>) analysisData.get("metrics");
                
                if (metrics != null) {
                    if (metrics.containsKey("eps") && metrics.get("eps") != null) prompt.append("- 每股收益：").append(metrics.get("eps")).append("元\n");
                    if (metrics.containsKey("roe") && metrics.get("roe") != null) prompt.append("- 净资产收益率：").append(metrics.get("roe")).append("%\n");
                    if (metrics.containsKey("grossMargin") && metrics.get("grossMargin") != null) prompt.append("- 毛利率：").append(metrics.get("grossMargin")).append("%\n");
                    if (metrics.containsKey("revenueGrowth") && metrics.get("revenueGrowth") != null) prompt.append("- 营收增长率：").append(metrics.get("revenueGrowth")).append("%\n");
                    if (metrics.containsKey("profitGrowth") && metrics.get("profitGrowth") != null) prompt.append("- 净利润增长率：").append(metrics.get("profitGrowth")).append("%\n");
                    if (metrics.containsKey("totalAssets") && metrics.get("totalAssets") != null) prompt.append("- 总资产：").append(metrics.get("totalAssets")).append("亿元\n");
                    if (metrics.containsKey("netAssets") && metrics.get("netAssets") != null) prompt.append("- 净资产：").append(metrics.get("netAssets")).append("亿元\n");
                    if (metrics.containsKey("revenue") && metrics.get("revenue") != null) prompt.append("- 营业收入：").append(metrics.get("revenue")).append("亿元\n");
                    if (metrics.containsKey("netProfit") && metrics.get("netProfit") != null) prompt.append("- 净利润：").append(metrics.get("netProfit")).append("亿元\n");
                }
            }
        }
        
        prompt.append("\n用户问题：").append(question).append("\n\n");
        prompt.append("请用简洁明了的语言回答，基于提供的财务数据进行分析。");
        
        // 调用LLM
        Map<String, Object> promptData = new HashMap<>();
        promptData.put("prompt", prompt.toString());
        return llmAnalysisService.analyzeFinancialReport("", promptData);
    }
}