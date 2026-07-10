package com.zhubao.manage.module.role.dto;

import javax.validation.constraints.Size;

public class RoleUpdateDTO {

    @Size(max = 100)
    private String roleName;

    private String dataScope;
    private String status;
    private String remark;

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getDataScope() { return dataScope; }
    public void setDataScope(String dataScope) { this.dataScope = dataScope; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
