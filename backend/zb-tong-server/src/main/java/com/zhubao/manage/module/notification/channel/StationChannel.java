package com.zhubao.manage.module.notification.channel;

import com.zhubao.manage.module.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 站内信通道 —— 直接写入 notification 表
 */
@Component
public class StationChannel {

    private static final Logger log = LoggerFactory.getLogger(StationChannel.class);
    private final NotificationService notificationService;

    public StationChannel(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void send(Long receiverId, String title, String content, String type, String businessType, Long businessId) {
        notificationService.send(receiverId, title, content, type, businessType, businessId);
        log.info("站内信已发送: receiver={}, title={}", receiverId, title);
    }
}
