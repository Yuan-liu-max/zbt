package com.zhubao.manage.module.human.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("employee_monthly_review")
public class EmployeeMonthlyReview {
    @TableId(type = IdType.AUTO) private Long id;
    private Long employeeId;
    private Long reviewerId;
    private String reviewMonth;
    private BigDecimal totalSalesAmount;
    private Integer salesOrderCount;
    private BigDecimal avgOrderAmount;
    private BigDecimal newCustomerSales;
    private BigDecimal oldCustomerRepurchaseSales;
    private String keyCategorySales;
    private BigDecimal serviceScore;
    private BigDecimal taskExecutionScore;
    private BigDecimal rewardAmount;
    private BigDecimal penaltyAmount;
    private String managerReview;
    @TableLogic private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
