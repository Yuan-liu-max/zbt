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
     * ROLE_STORE_MANAGER  → task.storeId 对应门店的店长 (store_manager_id)
     * ROLE_REGIONAL_MANAGER → task.storeId 所属区域的区域经理
     * 其他角色码           → 查 sys_user_role 中持有该角色的第一个用户
     * 无匹配              → 返回 null（管理员手动分配）
     */
    private Long resolveAuditor(TaskInstance task, TaskTemplate template) {
        String auditorRole = template.getDefaultAuditorRole();
        if (auditorRole == null || task.getStoreId() == null) return null;

        Store store = storeMapper.selectById(task.getStoreId());
        if (store == null) return null;

        // 1. 店长审核 → 直接取门店的 store_manager_id
        if ("ROLE_MANAGER".equals(auditorRole)) {
            return store.getStoreManagerId();
        }

        // 2. 区域经理审核 → 查同区域下持有 ROLE_REGIONAL 角色的用户
        if ("ROLE_REGIONAL".equals(auditorRole)) {
            return findRegionalManager(store.getRegionId());
        }

        // 3. 其他角色 → 查持有该角色的任意用户
        return findFirstByRole(auditorRole);
    }

    /**
     * 查找指定区域的区域经理：sys_user.region_id = ? 且角色包含 ROLE_REGIONAL
     */
    private Long findRegionalManager(Long regionId) {
        if (regionId == null) return null;
        try {
            com.zhubao.manage.module.role.entity.Role regionalRole = roleMapper.selectOne(
                    new LambdaQueryWrapper<com.zhubao.manage.module.role.entity.Role>()
                            .eq(com.zhubao.manage.module.role.entity.Role::getRoleCode, "ROLE_REGIONAL"));
            if (regionalRole == null) return null;

            List<UserRole> urList = userRoleMapper.selectList(
                    new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, regionalRole.getId()));
            List<Long> candidateIds = urList.stream().map(UserRole::getUserId).collect(java.util.stream.Collectors.toList());
            if (candidateIds.isEmpty()) return null;

            // 在这些候选人中找 region_id 匹配的
            List<User> users = userMapper.selectBatchIds(candidateIds);
            return users.stream()
                    .filter(u -> regionId.equals(u.getRegionId()))
                    .map(User::getId)
                    .findFirst().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private Long findFirstByRole(String roleCode) {
        try {
            com.zhubao.manage.module.role.entity.Role role = roleMapper.selectOne(
                    new LambdaQueryWrapper<com.zhubao.manage.module.role.entity.Role>()
                            .eq(com.zhubao.manage.module.role.entity.Role::getRoleCode, roleCode));
            if (role == null) return null;
            List<UserRole> urList = userRoleMapper.selectList(
                    new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, role.getId()));
            return urList.isEmpty() ? null : urList.get(0).getUserId();
        } catch (Exception ignored) { return null; }
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
