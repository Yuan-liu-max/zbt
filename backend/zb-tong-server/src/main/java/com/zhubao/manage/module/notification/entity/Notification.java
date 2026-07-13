package com.zhubao.manage.module.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO) private Long id;
    private Long receiverId;
    private String title;
    private String content;
    private String notificationType;
    private String businessType;
    private Long businessId;
    private Integer isRead;
    private LocalDateTime readAt;
    private String channel;
    private String sendStatus;
    @TableLogic private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
