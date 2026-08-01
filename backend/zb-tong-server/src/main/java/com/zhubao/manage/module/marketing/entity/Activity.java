package com.zhubao.manage.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("marketing_activity")
public class Activity {
    @TableId(type = IdType.AUTO) private Long id;
    private String name;
    private String type;
    private String startTime;
    private String endTime;
    private String status;
    private String scope;
    private Integer registeredCount;
    private Integer totalCount;
    @TableLogic private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
