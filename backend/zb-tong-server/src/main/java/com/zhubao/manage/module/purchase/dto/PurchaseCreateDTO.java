package com.zhubao.manage.module.purchase.dto;

import com.zhubao.manage.module.purchase.entity.PurchaseItem;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseCreateDTO {
    @NotBlank(message = "采购单号不能为空")
    private String orderNo;
    private Long storeId;
    private Long supplierId;
    private BigDecimal totalAmount;
    private String remark;
    @Valid
    private List<PurchaseItem> items;
}
