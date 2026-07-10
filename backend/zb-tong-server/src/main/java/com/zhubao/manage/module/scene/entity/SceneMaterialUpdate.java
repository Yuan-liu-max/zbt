package com.zhubao.manage.module.scene.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scene_material_update")
public class SceneMaterialUpdate {
    @TableId(type = IdType.AUTO) private Long id;
    private Long storeId;
    private Long checkerId;
    private LocalDate checkDate;
    private String materialType;
    private String currentStatus;
    private String updatedPhotos;
    private String issueDescription;
    private Integer replacementRequired;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
