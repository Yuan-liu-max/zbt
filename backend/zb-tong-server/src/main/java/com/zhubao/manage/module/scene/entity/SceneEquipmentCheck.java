package com.zhubao.manage.module.scene.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("scene_equipment_check")
public class SceneEquipmentCheck {
    @TableId(type = IdType.AUTO) private Long id;
    private Long storeId;
    private Long checkerId;
    private LocalDate checkDate;
    private String equipmentType;
    private String status;
    private String issueDescription;
    private Integer repairRequired;
    private String photoUrls;
    @TableLogic private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
