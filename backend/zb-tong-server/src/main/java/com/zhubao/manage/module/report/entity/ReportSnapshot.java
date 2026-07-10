package com.zhubao.manage.module.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("report_snapshot")
public class ReportSnapshot {
    @TableId(type = IdType.AUTO) private Long id;
    private String reportType;
    private String reportPeriod;
    private Long storeId;
    private String reportJson;
    private LocalDateTime generatedAt;
}
