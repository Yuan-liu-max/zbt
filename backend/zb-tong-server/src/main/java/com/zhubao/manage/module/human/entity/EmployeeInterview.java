package com.zhubao.manage.module.human.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("employee_interview")
public class EmployeeInterview {
    @TableId(type = IdType.AUTO) private Long id;
    private Long employeeId;
    private Long interviewerId;
    private LocalDate interviewDate;
    private BigDecimal currentWeekSales;
    private BigDecimal targetCompletionRate;
    private String mainProblem;
    private String customerFollowIssue;
    private String productKnowledgeGap;
    private String mindsetStatus;
    private BigDecimal nextWeekGoal;
    private String improvementPlan;
    private String managerComment;
    private String employeeFeedback;
    private LocalDate followUpDate;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
