package com.finance.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.entity.Announcement;
import com.finance.mapper.AnnouncementMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AnnouncementService extends ServiceImpl<AnnouncementMapper, Announcement> {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public List<Announcement> getAllAnnouncements() {
        List<Announcement> announcements = baseMapper.selectAllOrderByPublishDate();
        announcements.forEach(a -> {
            if (a.getPublishDate() != null) {
                a.setFormattedPublishDate(a.getPublishDate().format(FORMATTER));
            }
        });
        return announcements;
    }
    
    public Announcement getAnnouncementById(Long id) {
        Announcement announcement = baseMapper.selectById(id);
        if (announcement != null && announcement.getPublishDate() != null) {
            announcement.setFormattedPublishDate(announcement.getPublishDate().format(FORMATTER));
        }
        return announcement;
    }
    
    public boolean createAnnouncement(Announcement announcement) {
        announcement.setCreateTime(LocalDateTime.now());
        announcement.setUpdateTime(LocalDateTime.now());
        announcement.setPublishDate(LocalDateTime.now());
        return save(announcement);
    }
    
    public boolean updateAnnouncement(Long id, Announcement announcement) {
        Announcement existing = baseMapper.selectById(id);
        if (existing == null) {
            return false;
        }
        existing.setTitle(announcement.getTitle());
        existing.setContent(announcement.getContent());
        existing.setUrl(announcement.getUrl());
        existing.setUpdateTime(LocalDateTime.now());
        return updateById(existing);
    }
    
    public boolean deleteAnnouncement(Long id) {
        return removeById(id);
    }
}