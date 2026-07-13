package com.zhubao.manage.module.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("prompt_template")
public class PromptTemplate {
    @TableId(type = IdType.AUTO) private Long id;
    private String templateName;
    private String businessType;
    private String promptContent;
    private String inputSchema;
    private String outputSchema;
    private String modelName;
    private String status;
    private Long createdBy;
    @TableLogic private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
