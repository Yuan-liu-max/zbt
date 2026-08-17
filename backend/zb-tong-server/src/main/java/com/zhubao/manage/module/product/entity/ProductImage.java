package com.zhubao.manage.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@TableName("product_image")
public class ProductImage {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull
    private Long productId;

    @NotBlank
    private String imageUrl;

    private Integer sortOrder = 0;

    private Integer isPrimary = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
