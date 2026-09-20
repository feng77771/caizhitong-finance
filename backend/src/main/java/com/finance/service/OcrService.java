package com.finance.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class OcrService {

    private static final String TESSDATA_PATH = "D:/财报/CaiZhiTong/finance/backend/tessdata";
    private static final String TEMP_DIR = "D:/财报/CaiZhiTong/finance/backend/temp";

    /**
     * 从PDF文件中提取图片并进行OCR识别
     */
    public String extractTextFromImagePdf(String pdfPath) throws IOException {
        File file = new File(pdfPath);
        if (!file.exists()) {
            throw new IOException("PDF文件不存在: " + pdfPath);
        }

        // 确保临时目录存在
        Path tempPath = Paths.get(TEMP_DIR);
        if (!Files.exists(tempPath)) {
            Files.createDirectories(tempPath);
        }

        StringBuilder result = new StringBuilder();
        
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFRenderer renderer = new PDFRenderer(document);
            
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 300);
                
                // 临时保存图片
                File tempImage = new File(TEMP_DIR + "/page_" + i + ".png");
                ImageIO.write(image, "PNG", tempImage);
                
                // OCR识别
                String pageText = performOcr(tempImage.getAbsolutePath());
                result.append(pageText).append("\n\n");
                
                // 删除临时图片
                tempImage.delete();
            }
        }
        
        return result.toString();
    }

    /**
     * 对图片文件进行OCR识别
     */
    public String performOcr(String imagePath) {
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            return "";
        }

        ITesseract tesseract = new Tesseract();
        
        // 设置Tessdata路径
        File tessdataDir = new File(TESSDATA_PATH);
        if (tessdataDir.exists() && tessdataDir.isDirectory()) {
            tesseract.setDatapath(TESSDATA_PATH);
        }
        
        // 设置语言：英文+中文
        tesseract.setLanguage("eng+chi_sim");
        
        // 设置识别模式
        tesseract.setTessVariable("tessedit_char_whitelist", 
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" +
            "一二三四五六七八九十百千万亿元角分%￥$()（）,，.。%");
        
        try {
            return tesseract.doOCR(imageFile);
        } catch (TesseractException e) {
            System.err.println("OCR识别失败: " + e.getMessage());
            return "";
        }
    }

    /**
     * 判断PDF是否为扫描件（图片格式）
     */
    public boolean isImagePdf(String pdfPath) throws IOException {
        File file = new File(pdfPath);
        try (PDDocument document = Loader.loadPDF(file)) {
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                // 检查页面是否包含文本
                if (document.getPage(i).getContents() != null) {
                    return false; // 包含文本内容，不是扫描件
                }
            }
            return true; // 没有文本内容，可能是扫描件
        }
    }

    /**
     * 综合提取PDF文本（先尝试普通提取，失败则使用OCR）
     */
    public String extractText(String pdfPath) throws IOException {
        File file = new File(pdfPath);
        if (!file.exists()) {
            throw new IOException("PDF文件不存在: " + pdfPath);
        }

        // 首先尝试普通文本提取
        PdfParserService pdfParser = new PdfParserService();
        String text = pdfParser.extractText(pdfPath);
        
        // 如果提取的文本很少，可能是扫描件，使用OCR
        if (text.trim().length() < 100) {
            text = extractTextFromImagePdf(pdfPath);
        }
        
        return text;
    }
}