package com.zhubao.manage.module.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_chat_history")
public class AiChatHistory {
    @TableId(type = IdType.AUTO) private Long id;
    private String question;
    private String answer;
    private String modelName;
    @TableLogic private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
