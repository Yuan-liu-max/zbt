package com.zhubao.manage.module.human.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("employee_training")
public class EmployeeTraining {
    @TableId(type = IdType.AUTO) private Long id;
    private String trainingTitle;
    private String trainingType;
    private Long trainerId;
    private LocalDate trainingDate;
    private BigDecimal examScore;
    private String trainingSummary;
    private String materialUrls;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
