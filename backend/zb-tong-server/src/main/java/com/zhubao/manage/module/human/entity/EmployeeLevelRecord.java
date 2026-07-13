package com.zhubao.manage.module.human.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("employee_level_record")
public class EmployeeLevelRecord {
    @TableId(type = IdType.AUTO) private Long id;
    private Long employeeId;
    private String evalMonth;
    private BigDecimal performanceScore;
    private BigDecimal serviceScore;
    private BigDecimal executionScore;
    private String finalLevel;
    private String reason;
    private String nextMonthPlan;
    @TableLogic private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
