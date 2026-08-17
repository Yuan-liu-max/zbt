package com.zhubao.manage.module.auth.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class LoginResultDTO {

    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private String phone;
    private String email;
    private String timezone;
    private String language;
    private String dateFormat;
    private Boolean notifySystem;
    private Boolean notifyOrder;
    private Boolean notifyInventory;
    private Boolean notifyMarketing;
    private Long storeId;
    private String storeName;
    private Long regionId;
    private String regionName;
    private String position;
    private LocalDate entryDate;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private List<String> roles;
    private List<String> roleNames;
    private List<String> permissions;

    // ---- builder-style setters ----

    public LoginResultDTO token(String token) { this.token = token; return this; }
    public LoginResultDTO userId(Long id) { this.userId = id; return this; }
    public LoginResultDTO username(String v) { this.username = v; return this; }
    public LoginResultDTO realName(String v) { this.realName = v; return this; }
    public LoginResultDTO avatar(String v) { this.avatar = v; return this; }
    public LoginResultDTO phone(String v) { this.phone = v; return this; }
    public LoginResultDTO email(String v) { this.email = v; return this; }
    public LoginResultDTO timezone(String v) { this.timezone = v; return this; }
    public LoginResultDTO language(String v) { this.language = v; return this; }
    public LoginResultDTO dateFormat(String v) { this.dateFormat = v; return this; }
    public LoginResultDTO notifySystem(Boolean v) { this.notifySystem = v; return this; }
    public LoginResultDTO notifyOrder(Boolean v) { this.notifyOrder = v; return this; }
    public LoginResultDTO notifyInventory(Boolean v) { this.notifyInventory = v; return this; }
    public LoginResultDTO notifyMarketing(Boolean v) { this.notifyMarketing = v; return this; }
    public LoginResultDTO storeId(Long v) { this.storeId = v; return this; }
    public LoginResultDTO storeName(String v) { this.storeName = v; return this; }
    public LoginResultDTO regionId(Long v) { this.regionId = v; return this; }
    public LoginResultDTO regionName(String v) { this.regionName = v; return this; }
    public LoginResultDTO position(String v) { this.position = v; return this; }
    public LoginResultDTO entryDate(LocalDate v) { this.entryDate = v; return this; }
    public LoginResultDTO createdAt(LocalDateTime v) { this.createdAt = v; return this; }
    public LoginResultDTO lastLoginAt(LocalDateTime v) { this.lastLoginAt = v; return this; }
    public LoginResultDTO roles(List<String> v) { this.roles = v; return this; }
    public LoginResultDTO roleNames(List<String> v) { this.roleNames = v; return this; }
    public LoginResultDTO permissions(List<String> v) { this.permissions = v; return this; }

    // ---- getters ----

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRealName() { return realName; }
    public String getAvatar() { return avatar; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getTimezone() { return timezone; }
    public String getLanguage() { return language; }
    public String getDateFormat() { return dateFormat; }
    public Boolean getNotifySystem() { return notifySystem; }
    public Boolean getNotifyOrder() { return notifyOrder; }
    public Boolean getNotifyInventory() { return notifyInventory; }
    public Boolean getNotifyMarketing() { return notifyMarketing; }
    public List<String> getRoles() { return roles; }
    public List<String> getPermissions() { return permissions; }
    public String getStoreName() { return storeName; }
    public String getRegionName() { return regionName; }
    public String getPosition() { return position; }
    public LocalDate getEntryDate() { return entryDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public Long getStoreId() { return storeId; }
    public Long getRegionId() { return regionId; }
    public List<String> getRoleNames() { return roleNames; }
}
