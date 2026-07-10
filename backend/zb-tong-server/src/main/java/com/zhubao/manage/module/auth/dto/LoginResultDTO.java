package com.zhubao.manage.module.auth.dto;

import java.util.List;

public class LoginResultDTO {

    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private List<String> roles;
    private List<String> permissions;

    // ---- builder-style setters ----

    public LoginResultDTO token(String token) { this.token = token; return this; }
    public LoginResultDTO userId(Long id) { this.userId = id; return this; }
    public LoginResultDTO username(String v) { this.username = v; return this; }
    public LoginResultDTO realName(String v) { this.realName = v; return this; }
    public LoginResultDTO avatar(String v) { this.avatar = v; return this; }
    public LoginResultDTO roles(List<String> v) { this.roles = v; return this; }
    public LoginResultDTO permissions(List<String> v) { this.permissions = v; return this; }

    // ---- getters ----

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRealName() { return realName; }
    public String getAvatar() { return avatar; }
    public List<String> getRoles() { return roles; }
    public List<String> getPermissions() { return permissions; }
}
