package com.zhubao.manage.module.scene.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scene_health_inspection")
public class SceneHealthInspection {
    @TableId(type = IdType.AUTO) private Long id;
    private String inspectionTime;
    private LocalDate inspectionDate;
    private Long inspectorId;
    private Long storeId;
    private String areaResults;
    private String issueDescription;
    private String photoUrls;
    private Integer rectificationRequired;
    @TableLogic private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
