package com.zhubao.manage.module.purchase.dto;

import com.zhubao.manage.common.dto.PageDTO;

public class PurchaseQueryDTO extends PageDTO {
    private String purchaseNo;
    private String status;
    private String orderNo;

    public String getPurchaseNo() { return purchaseNo; }
    public void setPurchaseNo(String v) { this.purchaseNo = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String v) { this.orderNo = v; }
}
