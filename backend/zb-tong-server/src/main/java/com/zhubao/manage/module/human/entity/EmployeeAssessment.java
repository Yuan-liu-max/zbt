package com.zhubao.manage.module.human.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("employee_assessment")
public class EmployeeAssessment {
    @TableId(type = IdType.AUTO) private Long id;
    private Long employeeId;
    private Long assessorId;
    private String assessmentWeek;
    private BigDecimal productKnowledgeScore;
    private BigDecimal matchingSkillScore;
    private BigDecimal receptionScore;
    private BigDecimal objectionHandlingScore;
    private BigDecimal promotionScriptScore;
    private BigDecimal totalScore;
    private String improvementAdvice;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
