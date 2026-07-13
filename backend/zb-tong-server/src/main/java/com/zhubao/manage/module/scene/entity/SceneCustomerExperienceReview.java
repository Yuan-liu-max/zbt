package com.zhubao.manage.module.scene.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("scene_customer_experience_review")
public class SceneCustomerExperienceReview {
    @TableId(type = IdType.AUTO) private Long id;
    private Long storeId;
    private Long reviewerId;
    private String reviewWeek;
    private Integer feedbackCount;
    private Integer complaintCount;
    private String commonFeedback;
    private String improvementPlan;
    private Long responsiblePersonId;
    @TableLogic private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
