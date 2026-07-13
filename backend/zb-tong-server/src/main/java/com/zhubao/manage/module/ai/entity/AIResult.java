package com.zhubao.manage.module.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_result")
public class AIResult {
    @TableId(type = IdType.AUTO) private Long id;
    private String businessType;
    private Long relatedId;
    private Long promptTemplateId;
    private String inputSnapshot;
    private String outputText;
    private String outputJson;
    private BigDecimal score;
    private String modelName;
    private String tokenUsage;
    private String status;
    @TableLogic private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
