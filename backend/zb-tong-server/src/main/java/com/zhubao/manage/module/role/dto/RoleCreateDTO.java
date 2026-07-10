package com.zhubao.manage.module.role.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class RoleCreateDTO {

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50)
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 100)
    private String roleName;

    @NotBlank(message = "数据权限不能为空")
    private String dataScope;

    private String remark;

    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getDataScope() { return dataScope; }
    public void setDataScope(String dataScope) { this.dataScope = dataScope; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
