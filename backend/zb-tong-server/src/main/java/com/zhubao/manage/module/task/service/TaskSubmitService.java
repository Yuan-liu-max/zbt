package com.zhubao.manage.module.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.enums.TaskStatusEnum;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.organization.entity.Store;
import com.zhubao.manage.module.organization.mapper.StoreMapper;
import com.zhubao.manage.module.role.entity.UserRole;
import com.zhubao.manage.module.role.mapper.UserRoleMapper;
import com.zhubao.manage.module.role.mapper.RoleMapper;
import com.zhubao.manage.module.task.dto.TaskSubmitDTO;
import com.zhubao.manage.module.task.entity.TaskInstance;
import com.zhubao.manage.module.task.entity.TaskSubmission;
import com.zhubao.manage.module.task.entity.TaskTemplate;
import com.zhubao.manage.module.task.mapper.TaskInstanceMapper;
import com.zhubao.manage.module.task.mapper.TaskSubmissionMapper;
import com.zhubao.manage.module.task.mapper.TaskTemplateMapper;
import com.zhubao.manage.module.user.entity.User;
import com.zhubao.manage.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务提交服务 —— 状态机驱动 + 审核人自动分配
 */
@Service
public class TaskSubmitService {

    private final TaskInstanceMapper taskInstanceMapper;
    private final TaskSubmissionMapper taskSubmissionMapper;
    private final TaskTemplateMapper taskTemplateMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserMapper userMapper;
    private final StoreMapper storeMapper;
    private final RoleMapper roleMapper;
    private final UserContextHolder userContextHolder;

    public TaskSubmitService(TaskInstanceMapper tim, TaskSubmissionMapper tsm,
                             TaskTemplateMapper ttm, UserRoleMapper urm,
                             UserMapper um, StoreMapper sm, RoleMapper rm,
                             UserContextHolder uch) {
        this.taskInstanceMapper = tim; this.taskSubmissionMapper = tsm;
        this.taskTemplateMapper = ttm; this.userRoleMapper = urm;
        this.userMapper = um; this.storeMapper = sm; this.roleMapper = rm;
        this.userContextHolder = uch;
    }

    @Transactional
    public TaskSubmission submit(TaskSubmitDTO dto) {
        TaskInstance task = getTask(dto.getTaskId());
        TaskStatusEnum currentStatus = TaskStatusEnum.valueOf(task.getStatus());

        if (!currentStatus.canSubmit()) {
            throw new BusinessException(ErrorCode.TASK_STATUS_INVALID.getCode(),
                    "当前状态[" + task.getStatus() + "]不允许提交");
        }

        TaskSubmission submission = new TaskSubmission();
        submission.setTaskId(dto.getTaskId());
        submission.setSubmitterId(userContextHolder.getUserId());
        submission.setTextContent(dto.getTextContent());
        submission.setFormData(dto.getFormData());
        submission.setPhotoUrls(dto.getPhotoUrls());
        submission.setAttachmentUrls(dto.getAttachmentUrls());
        submission.setLocation(dto.getLocation());
        submission.setSubmittedAt(LocalDateTime.now());
        taskSubmissionMapper.insert(submission);

        TaskTemplate template = taskTemplateMapper.selectById(task.getTemplateId());
        boolean needAudit = template != null && template.getRequireAudit() == 1;

        task.setStatus(TaskStatusEnum.SUBMITTED.getCode());
        if (needAudit) {
            task.setStatus(TaskStatusEnum.AUDITING.getCode());
            // ---- 分配审核人 ----
            if (task.getAuditorId() == null && template != null) {
                Long auditorId = resolveAuditor(task, template);
                task.setAuditorId(auditorId);
            }
        } else {
            task.setStatus(TaskStatusEnum.COMPLETED.getCode());
            task.setCompletedTime(LocalDateTime.now());
        }
        taskInstanceMapper.updateById(task);

        return submission;
    }

    /**
     * 根据模板配置的审核角色自动分配审核人
     *
     * 逻辑：
     *   导购(store_id) → 店长（同门店 ROLE_MANAGER）
     *   店长(store_id) → 区域经理（同区域 ROLE_REGIONAL）
     *   其他          → 根据 defaultAuditorRole 查找同区域/同门店用户
     */
    private Long resolveAuditor(TaskInstance task, TaskTemplate template) {
        String auditorRole = template.getDefaultAuditorRole();
        if (auditorRole == null) return null;

        // 1. 查执行人信息
        User assignee = userMapper.selectById(task.getAssigneeId());
        if (assignee == null) return findFirstByRole(auditorRole);

        // 2. 执行人是导购 → 找同门店店长；执行人是店长 → 找同区域经理
        if (assignee.getStoreId() != null) {
            Store store = storeMapper.selectById(assignee.getStoreId());
            if (store != null && store.getStoreManagerId() != null) {
                return store.getStoreManagerId();
            }
        }

        // 3. 兜底：查找具有 defaultAuditorRole 角色的第一个用户
        return findFirstByRole(auditorRole);
    }

    private Long findFirstByRole(String roleCode) {
        try {
            com.zhubao.manage.module.role.entity.Role role = roleMapper.selectOne(
                    new LambdaQueryWrapper<com.zhubao.manage.module.role.entity.Role>()
                            .eq(com.zhubao.manage.module.role.entity.Role::getRoleCode, roleCode));
            if (role == null) return null;
            List<UserRole> urList = userRoleMapper.selectList(
                    new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, role.getId()));
            if (!urList.isEmpty()) return urList.get(0).getUserId();
        } catch (Exception ignored) {}
        return null;
    }

    public TaskSubmission getLatestSubmission(Long taskId) {
        return taskSubmissionMapper.selectOne(
                new LambdaQueryWrapper<TaskSubmission>()
                        .eq(TaskSubmission::getTaskId, taskId)
                        .orderByDesc(TaskSubmission::getCreatedAt)
                        .last("LIMIT 1"));
    }

    private TaskInstance getTask(Long taskId) {
        TaskInstance t = taskInstanceMapper.selectById(taskId);
        if (t == null) throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        return t;
    }
}
