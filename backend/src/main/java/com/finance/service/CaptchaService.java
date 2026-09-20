package com.finance.service;

import com.finance.util.CaptchaUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

    private static final int CAPTCHA_EXPIRE_MINUTES = 5;
    private static final int CAPTCHA_LENGTH = 4;
    
    // 使用内存缓存存储验证码（生产环境建议使用Redis）
    private static final ConcurrentHashMap<String, CaptchaItem> captchaCache = new ConcurrentHashMap<>();
    
    private static class CaptchaItem {
        String code;
        long expireTime;
        
        CaptchaItem(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }

    public Map<String, String> generateCaptcha() {
        String code = CaptchaUtil.generateCode(CAPTCHA_LENGTH);
        String uuid = UUID.randomUUID().toString();
        
        // 存储到内存缓存，5分钟过期
        long expireTime = System.currentTimeMillis() + (CAPTCHA_EXPIRE_MINUTES * 60 * 1000);
        captchaCache.put(uuid, new CaptchaItem(code, expireTime));
        
        String imageBase64 = CaptchaUtil.generateImageBase64(code);
        
        Map<String, String> result = new HashMap<>();
        result.put("uuid", uuid);
        result.put("image", imageBase64);
        
        return result;
    }

    public boolean validateCaptcha(String uuid, String captcha) {
        CaptchaItem item = captchaCache.get(uuid);
        
        if (item == null) {
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > item.expireTime) {
            captchaCache.remove(uuid);
            return false;
        }
        
        boolean isValid = item.code.equalsIgnoreCase(captcha);
        
        // 验证成功后删除验证码
        if (isValid) {
            captchaCache.remove(uuid);
        }
        
        return isValid;
    }

    public void removeCaptcha(String uuid) {
        captchaCache.remove(uuid);
    }
    
    // 定时清理过期验证码（每分钟执行一次）
    @Scheduled(fixedRate = 60000)
    public void cleanExpiredCaptchas() {
        long now = System.currentTimeMillis();
        captchaCache.entrySet().removeIf(entry -> entry.getValue().expireTime < now);
    }
}