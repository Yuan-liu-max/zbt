package com.zhubao.manage.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("promotion")
public class Promotion {
    @TableId(type = IdType.AUTO) private Long id;
    private String name;
    private String type;
    private String discountMethod;
    private String startTime;
    private String endTime;
    private String status;
    private String scope;
    private Integer usageCount;
    @TableLogic private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
