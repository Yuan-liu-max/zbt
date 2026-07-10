package com.zhubao.manage.module.scene.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scene_display_inspection")
public class SceneDisplayInspection {
    @TableId(type = IdType.AUTO) private Long id;
    private LocalDate inspectionDate;
    private Long storeId;
    private Long inspectorId;
    private String displayArea;
    private BigDecimal standardScore;
    private String issueDescription;
    private String beforePhotos;
    private String afterPhotos;
    private String rectificationPlan;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
