package com.finance.controller;

import com.finance.dto.ApiResponse;
import com.finance.entity.Announcement;
import com.finance.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/announcements")
public class AnnouncementController {
    
    @Autowired
    private AnnouncementService announcementService;
    
    @GetMapping
    public ApiResponse<List<Announcement>> getAllAnnouncements() {
        List<Announcement> announcements = announcementService.getAllAnnouncements();
        return ApiResponse.success(announcements);
    }
    
    @GetMapping("/latest")
    public ApiResponse<List<Announcement>> getLatestAnnouncements() {
        List<Announcement> announcements = announcementService.getAllAnnouncements();
        // 返回最新的5条公告
        if (announcements.size() > 5) {
            announcements = announcements.subList(0, 5);
        }
        return ApiResponse.success(announcements);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Announcement> getAnnouncementById(@PathVariable Long id) {
        Announcement announcement = announcementService.getAnnouncementById(id);
        if (announcement == null) {
            return ApiResponse.error("公告不存在");
        }
        return ApiResponse.success(announcement);
    }
    
    @PostMapping
    public ApiResponse<String> createAnnouncement(@RequestBody Announcement announcement) {
        boolean success = announcementService.createAnnouncement(announcement);
        if (success) {
            return ApiResponse.success("创建成功");
        }
        return ApiResponse.error("创建失败");
    }
    
    @PutMapping("/{id}")
    public ApiResponse<String> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        boolean success = announcementService.updateAnnouncement(id, announcement);
        if (success) {
            return ApiResponse.success("更新成功");
        }
        return ApiResponse.error("更新失败");
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteAnnouncement(@PathVariable Long id) {
        boolean success = announcementService.deleteAnnouncement(id);
        if (success) {
            return ApiResponse.success("删除成功");
        }
        return ApiResponse.error("删除失败");
    }
}