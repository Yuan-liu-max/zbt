package com.zhubao.manage.common.enums;

/**
 * 任务实例状态枚举 —— 12种状态 + 状态机流转规则
 *
 * <pre>
 * 正常流转:
 *   PENDING → READY → IN_PROGRESS → SUBMITTED → (无需审核 → COMPLETED)
 *                                              → (需审核 → AUDITING → APPROVED → COMPLETED)
 *                                                                  → REJECTED → RECTIFYING ─┐
 *                                                                  → RECTIFY → RECTIFYING ───┤
 *                                                                       ↑____________________┘ (重新提交)
 * 终止态:
 *   PENDING/READY/IN_PROGRESS → CANCELLED
 *   任意状态(管理员) → VOIDED
 *   PENDING/READY/IN_PROGRESS (超时) → OVERDUE
 *   审批阶段 → EXEMPTED
 * </pre>
 */
public enum TaskStatusEnum {

    PENDING("PENDING", "待开始"),
    READY("READY", "可执行"),
    IN_PROGRESS("IN_PROGRESS", "执行中"),
    SUBMITTED("SUBMITTED", "已提交"),
    AUDITING("AUDITING", "审核中"),
    APPROVED("APPROVED", "已通过"),
    COMPLETED("COMPLETED", "已完成"),
    REJECTED("REJECTED", "已驳回"),
    RECTIFYING("RECTIFYING", "整改中"),
    OVERDUE("OVERDUE", "已超时"),
    CANCELLED("CANCELLED", "已取消"),
    VOIDED("VOIDED", "已作废"),
    EXEMPTED("EXEMPTED", "已豁免");

    private final String code;
    private final String desc;

    TaskStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    // ---- 状态流转合法性检查 ----

    /** 可否取消 */
    public boolean canCancel() {
        return this == PENDING || this == READY || this == IN_PROGRESS;
    }

    /** 可否作废（管理员强制） */
    public boolean canVoid() {
        return this != COMPLETED && this != CANCELLED && this != VOIDED;
    }

    /** 可否提交 */
    public boolean canSubmit() {
        return this == IN_PROGRESS || this == RECTIFYING;
    }

    /** 可否审核 */
    public boolean canAudit() {
        return this == SUBMITTED;
    }

    /** 是否需要审核 */
    public static boolean needsAudit(String status) {
        return SUBMITTED.code.equals(status);
    }

    /** 是否为终态 */
    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED || this == VOIDED || this == EXEMPTED;
    }
}
