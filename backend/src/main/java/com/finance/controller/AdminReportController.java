package com.finance.controller;

import com.finance.dto.ApiResponse;
import com.finance.entity.FinancialReport;
import com.finance.service.FinancialReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class UpdateReportRequest {
    private String companyName;
    private String companyCode;
    private String reportType;
    private String reportYear;
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getReportYear() { return reportYear; }
    public void setReportYear(String reportYear) { this.reportYear = reportYear; }
}

@RestController
@RequestMapping("/admin/reports")
@CrossOrigin
public class AdminReportController {

    @Autowired
    private FinancialReportService reportService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<FinancialReport>> getAllReports() {
        try {
            List<FinancialReport> reports = reportService.getAllReports();
            return ApiResponse.success(reports);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取财报列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FinancialReport> uploadReport(
            @RequestParam("companyName") String companyName,
            @RequestParam("companyCode") String companyCode,
            @RequestParam("reportType") String reportType,
            @RequestParam("reportYear") String reportYear,
            @RequestParam("file") MultipartFile file) {
        try {
            FinancialReport report = reportService.uploadReport(companyName, companyCode, reportType, reportYear, file);
            return ApiResponse.success(report);
        } catch (Exception e) {
            return ApiResponse.error(500, "上传财报失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/analyze")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> analyzeReport(@PathVariable Long id) {
        try {
            Map<String, Object> result = reportService.analyzeReport(id);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(500, "分析财报失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FinancialReport> updateReport(
            @PathVariable Long id,
            @RequestParam("companyName") String companyName,
            @RequestParam("companyCode") String companyCode,
            @RequestParam("reportType") String reportType,
            @RequestParam("reportYear") String reportYear,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            FinancialReport report = reportService.updateReport(id, companyName, companyCode, reportType, reportYear, file);
            return ApiResponse.success(report);
        } catch (Exception e) {
            return ApiResponse.error(500, "更新财报失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> getReportPdf(@PathVariable Long id) throws IOException {
        FinancialReport report = reportService.getReportById(id);
        if (report == null || report.getPdfPath() == null) {
            return ResponseEntity.notFound().build();
        }
        
        File file = new File(report.getPdfPath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        byte[] pdfContent = java.nio.file.Files.readAllBytes(file.toPath());
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(pdfContent);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteReport(@PathVariable Long id) {
        try {
            reportService.deleteReport(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(500, "删除财报失败: " + e.getMessage());
        }
    }
}