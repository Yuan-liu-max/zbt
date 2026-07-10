package com.zhubao.manage.module.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("store_monthly_score")
public class StoreMonthlyScore {
    @TableId(type = IdType.AUTO) private Long id;
    private Long storeId;
    private String scoreMonth;
    private BigDecimal totalScore;
    private BigDecimal humanScore;
    private BigDecimal productScore;
    private BigDecimal sceneScore;
    private BigDecimal disciplineScore;
    private Integer overdueCount;
    private Integer rejectedCount;
    private Integer ranking;
    private String detailJson;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
