package com.zhubao.manage.module.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.enums.TaskStatusEnum;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.task.dto.TaskAuditDTO;
import com.zhubao.manage.module.task.entity.TaskAudit;
import com.zhubao.manage.module.task.entity.TaskInstance;
import com.zhubao.manage.module.task.entity.TaskSubmission;
import com.zhubao.manage.module.task.mapper.TaskAuditMapper;
import com.zhubao.manage.module.task.mapper.TaskInstanceMapper;
import com.zhubao.manage.module.task.mapper.TaskSubmissionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 任务审核服务 —— 状态流转:
 *   AUDITING → APPROVED → COMPLETED
 *   AUDITING → REJECTED → RECTIFYING → (整改后重新提交 → SUBMITTED → AUDITING)
 *   AUDITING → RECTIFY  → RECTIFYING → (同上)
 */
@Service
public class TaskAuditService {

    private final TaskInstanceMapper taskInstanceMapper;
    private final TaskAuditMapper taskAuditMapper;
    private final TaskSubmissionMapper taskSubmissionMapper;
    private final UserContextHolder userContextHolder;

    public TaskAuditService(TaskInstanceMapper taskInstanceMapper,
                            TaskAuditMapper taskAuditMapper,
                            TaskSubmissionMapper taskSubmissionMapper,
                            UserContextHolder userContextHolder) {
        this.taskInstanceMapper = taskInstanceMapper;
        this.taskAuditMapper = taskAuditMapper;
        this.taskSubmissionMapper = taskSubmissionMapper;
        this.userContextHolder = userContextHolder;
    }

    /**
     * 审核 —— APPROVED / REJECTED / RECTIFY
     */
    @Transactional
    public TaskAudit audit(TaskAuditDTO dto) {
        TaskInstance task = getTask(dto.getTaskId());

        // 审核人身份校验
        if (task.getAuditorId() != null
                && !java.util.Objects.equals(userContextHolder.getUserId(), task.getAuditorId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "无权审核此任务");
        }

        // 只能在 AUDITING 状态下审核
        if (!TaskStatusEnum.AUDITING.getCode().equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.TASK_STATUS_INVALID.getCode(),
                    "当前状态[" + task.getStatus() + "]不允许审核");
        }

        // 查找最新提交记录
        LambdaQueryWrapper<TaskSubmission> subQw = new LambdaQueryWrapper<>();
        subQw.eq(TaskSubmission::getTaskId, dto.getTaskId());
        subQw.orderByDesc(TaskSubmission::getCreatedAt);
        subQw.last("LIMIT 1");
        TaskSubmission submission = taskSubmissionMapper.selectOne(subQw);

        // 保存审核记录
        TaskAudit audit = new TaskAudit();
        audit.setTaskId(dto.getTaskId());
        audit.setSubmissionId(submission != null ? submission.getId() : null);
        audit.setAuditorId(userContextHolder.getUserId());
        audit.setAuditResult(dto.getAuditResult());
        audit.setAuditComment(dto.getAuditComment());
        audit.setScore(dto.getScore());
        audit.setAuditedAt(LocalDateTime.now());
        taskAuditMapper.insert(audit);

        // 更新任务状态
        switch (dto.getAuditResult()) {
            case "APPROVED":
                task.setStatus(TaskStatusEnum.COMPLETED.getCode());
                task.setCompletedTime(LocalDateTime.now());
                task.setManualScore(dto.getScore());
                break;
            case "REJECTED":
                task.setStatus(TaskStatusEnum.RECTIFYING.getCode());
                break;
            case "RECTIFY":
                task.setStatus(TaskStatusEnum.RECTIFYING.getCode());
                break;
            default:
                throw new BusinessException(400, "无效的审核结果: " + dto.getAuditResult());
        }

        taskInstanceMapper.updateById(task);
        return audit;
    }

    /** 查询任务的审核记录 */
    public java.util.List<TaskAudit> getAuditHistory(Long taskId) {
        return taskAuditMapper.selectList(
                new LambdaQueryWrapper<TaskAudit>()
                        .eq(TaskAudit::getTaskId, taskId)
                        .orderByDesc(TaskAudit::getAuditedAt));
    }

    private TaskInstance getTask(Long taskId) {
        TaskInstance t = taskInstanceMapper.selectById(taskId);
        if (t == null) throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        return t;
    }
}
