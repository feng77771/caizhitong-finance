package com.finance.controller;

import com.finance.dto.ApiResponse;
import com.finance.entity.FinancialReport;
import com.finance.service.FinancialReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "*")
public class FinancialReportController {
    
    @Autowired
    private FinancialReportService financialReportService;
    
    @GetMapping("/list")
    public ApiResponse<List<FinancialReport>> getAllReports() {
        try {
            List<FinancialReport> reports = financialReportService.getAllReports();
            return ApiResponse.success("获取财报列表成功", reports);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/stock/{stockCode}")
    public ApiResponse<List<FinancialReport>> getReportsByStockCode(@PathVariable String stockCode) {
        try {
            List<FinancialReport> reports = financialReportService.getReportsByStockCode(stockCode);
            return ApiResponse.success("获取财报列表成功", reports);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{stockCode}/{year}")
    public ApiResponse<FinancialReport> getReport(@PathVariable String stockCode, @PathVariable Integer year) {
        try {
            FinancialReport report = financialReportService.getReportByStockCodeAndYear(stockCode, year);
            if (report != null) {
                return ApiResponse.success("获取财报成功", report);
            } else {
                return ApiResponse.error("未找到该财报");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/upload")
    public ApiResponse<String> uploadReport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("stockCode") String stockCode,
            @RequestParam("stockName") String stockName,
            @RequestParam("reportYear") Integer reportYear) {
        try {
            boolean success = financialReportService.uploadReport(file, stockCode, stockName, reportYear);
            if (success) {
                return ApiResponse.success("上传成功", null);
            } else {
                return ApiResponse.error("上传失败");
            }
        } catch (IOException e) {
            return ApiResponse.error("上传失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteReport(@PathVariable Long id) {
        try {
            boolean success = financialReportService.deleteReport(id);
            if (success) {
                return ApiResponse.success("删除成功", null);
            } else {
                return ApiResponse.error("删除失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/download/{id}")
    public ApiResponse<String> getDownloadPath(@PathVariable Long id) {
        try {
            String pdfPath = financialReportService.getReportPdfPath(id);
            if (pdfPath != null) {
                return ApiResponse.success("获取下载路径成功", pdfPath);
            } else {
                return ApiResponse.error("未找到该财报");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 测试接口
     */
    @GetMapping("/test")
    public String test() {
        System.out.println("测试接口被调用");
        return "test success";
    }
    
    /**
     * 下载PDF文件（直接返回文件流）
     */
    @GetMapping("/download/file/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        System.out.println("收到下载请求，ID: " + id);
        try {
            String pdfPath = financialReportService.getReportPdfPath(id);
            if (pdfPath == null) {
                System.out.println("PDF路径为空");
                return ResponseEntity.notFound().build();
            }
            
            File file = new File(pdfPath);
            if (!file.exists()) {
                System.out.println("文件不存在: " + pdfPath);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("文件存在，准备下载: " + pdfPath);
            byte[] fileContent = java.nio.file.Files.readAllBytes(file.toPath());
            String fileName = file.getName();
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                    .body(fileContent);
        } catch (Exception e) {
            System.out.println("下载文件失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 分析财报：PDF解析 + OCR + LLM + 向量检索
     */
    @GetMapping("/analyze/{stockCode}/{year}")
    public ApiResponse<Map<String, Object>> analyzeReport(
            @PathVariable String stockCode, 
            @PathVariable Integer year) {
        try {
            Map<String, Object> result = financialReportService.analyzeReportByStockCodeAndYear(stockCode, year);
            return ApiResponse.success("分析完成", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 根据ID分析财报
     */
    @GetMapping("/analyze/id/{id}")
    public ApiResponse<Map<String, Object>> analyzeReportById(@PathVariable Long id) {
        try {
            Map<String, Object> result = financialReportService.analyzeReport(id);
            return ApiResponse.success("分析完成", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * AI对话：基于财报数据回答问题
     */
    @PostMapping("/chat")
    public ApiResponse<String> chatWithAi(
            @RequestParam("stockCode") String stockCode,
            @RequestParam("reportYear") Integer reportYear,
            @RequestParam("question") String question) {
        try {
            String answer = financialReportService.chatWithAi(stockCode, reportYear, question);
            return ApiResponse.success("回答完成", answer);
        } catch (Exception e) {
            return ApiResponse.error("对话失败: " + e.getMessage());
        }
    }
}
