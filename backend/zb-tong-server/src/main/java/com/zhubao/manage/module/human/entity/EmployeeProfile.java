package com.zhubao.manage.module.human.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("employee_profile")
public class EmployeeProfile {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private Long storeId;
    private String position;
    private LocalDate entryDate;
    private String level;
    private String status;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
