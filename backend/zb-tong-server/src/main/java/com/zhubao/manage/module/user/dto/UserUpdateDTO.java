package com.zhubao.manage.module.user.dto;

import javax.validation.constraints.Size;
import java.util.List;

public class UserUpdateDTO {

    @Size(max = 50)
    private String realName;

    private String phone;
    private Long storeId;
    private Long regionId;
    private String position;
    private String entryDate;
    private String status;
    private List<Long> roleIds;

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public Long getRegionId() { return regionId; }
    public void setRegionId(Long regionId) { this.regionId = regionId; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getEntryDate() { return entryDate; }
    public void setEntryDate(String entryDate) { this.entryDate = entryDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
}
