package com.zhubao.manage.module.human.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("employee_training_record")
public class EmployeeTrainingRecord {
    @TableId(type = IdType.AUTO) private Long id;
    private Long trainingId;
    private Long employeeId;
    private String signInStatus;
    private BigDecimal examScore;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
