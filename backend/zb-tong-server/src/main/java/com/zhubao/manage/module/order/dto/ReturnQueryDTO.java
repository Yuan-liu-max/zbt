package com.zhubao.manage.module.order.dto;

import com.zhubao.manage.common.dto.PageDTO;

public class ReturnQueryDTO extends PageDTO {
    private String keyword;
    private String returnType;
    private String status;
    private String startDate;
    private String endDate;

    public String getKeyword() { return keyword; } public void setKeyword(String v) { this.keyword = v; }
    public String getReturnType() { return returnType; } public void setReturnType(String v) { this.returnType = v; }
    public String getStatus() { return status; } public void setStatus(String v) { this.status = v; }
    public String getStartDate() { return startDate; } public void setStartDate(String v) { this.startDate = v; }
    public String getEndDate() { return endDate; } public void setEndDate(String v) { this.endDate = v; }
}
