package com.zhubao.manage.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@TableName("product_brand")
public class ProductBrand {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank
    private String name;

    private String categoryName;

    private String logo;

    private String origin;

    private String description;

    @JsonAlias("sort")
    private Integer sortOrder = 0;

    private String status = "ENABLED";

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
