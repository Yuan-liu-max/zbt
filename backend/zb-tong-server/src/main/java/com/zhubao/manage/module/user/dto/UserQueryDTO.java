package com.zhubao.manage.module.user.dto;

import com.zhubao.manage.common.dto.PageDTO;

public class UserQueryDTO extends PageDTO {

    private String keyword;
    private Long storeId;
    private String status;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
