package com.zhubao.manage.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("product_sales_analysis")
public class ProductSalesAnalysis {
    @TableId(type = IdType.AUTO) private Long id;
    private String analysisWeek;
    private Long storeId;
    private Long analyzerId;
    private String hotProducts;
    private String normalProducts;
    private String slowProducts;
    private String noSales7Days;
    private String stockoutRiskProducts;
    private String analysisSummary;
    private String actionPlan;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
