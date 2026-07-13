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
    @javax.validation.constraints.NotNull(message = "检查日期不能为空") private LocalDate inspectionDate;
    private Long storeId;
    private Long inspectorId;
    @javax.validation.constraints.NotBlank(message = "陈列区域不能为空") private String displayArea;
    private BigDecimal standardScore;
    private String issueDescription;
    private String beforePhotos;
    private String afterPhotos;
    private String rectificationPlan;
    @TableLogic private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
