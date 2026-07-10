package com.zhubao.manage.module.role.dto;

import javax.validation.constraints.NotEmpty;
import java.util.List;

public class AssignDataScopeDTO {

    @NotEmpty(message = "数据权限配置不能为空")
    private List<ScopeItem> scopes;

    public List<ScopeItem> getScopes() { return scopes; }
    public void setScopes(List<ScopeItem> scopes) { this.scopes = scopes; }

    public static class ScopeItem {
        private String scopeType;
        private Long scopeValue;

        public String getScopeType() { return scopeType; }
        public void setScopeType(String scopeType) { this.scopeType = scopeType; }
        public Long getScopeValue() { return scopeValue; }
        public void setScopeValue(Long scopeValue) { this.scopeValue = scopeValue; }
    }
}
