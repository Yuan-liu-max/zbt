package com.zhubao.manage.module.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.notification.entity.Notification;
import com.zhubao.manage.module.notification.mapper.NotificationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationMapper nm) { this.notificationMapper = nm; }

    public IPage<Notification> page(Long receiverId, PageDTO dto, Integer isRead) {
        LambdaQueryWrapper<Notification> w = new LambdaQueryWrapper<Notification>()
                .eq(receiverId != null, Notification::getReceiverId, receiverId);
        if (isRead != null) w.eq(Notification::getIsRead, isRead);
        w.orderByDesc(Notification::getCreatedAt);
        return notificationMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), w);
    }

    @Transactional
    public void markRead(Long id, Long currentUserId) {
        Notification n = notificationMapper.selectById(id);
        if (n == null) return;
        // 归属校验：只能读自己的通知
        if (!n.getReceiverId().equals(currentUserId)) {
            throw new com.zhubao.manage.common.exception.BusinessException(403, "无权操作此通知");
        }
        n.setIsRead(1); n.setReadAt(LocalDateTime.now()); notificationMapper.updateById(n);
    }

    @Transactional
    public void markAllRead(Long receiverId) {
        java.util.List<Notification> list = notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>().eq(Notification::getReceiverId, receiverId).eq(Notification::getIsRead, 0));
        for (Notification n : list) { n.setIsRead(1); n.setReadAt(LocalDateTime.now()); notificationMapper.updateById(n); }
    }

    public long unreadCount(Long receiverId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>().eq(Notification::getReceiverId, receiverId).eq(Notification::getIsRead, 0));
    }

    /** 发送站内信 */
    @Transactional
    public Notification send(Long receiverId, String title, String content, String type, String businessType, Long businessId) {
        Notification n = new Notification();
        n.setReceiverId(receiverId); n.setTitle(title); n.setContent(content);
        n.setNotificationType(type); n.setBusinessType(businessType); n.setBusinessId(businessId);
        n.setIsRead(0); n.setChannel("STATION"); n.setSendStatus("SUCCESS");
        notificationMapper.insert(n);
        return n;
    }
}
