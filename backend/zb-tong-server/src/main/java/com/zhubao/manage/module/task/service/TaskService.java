package com.zhubao.manage.module.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.enums.TaskStatusEnum;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.module.organization.entity.Store;
import com.zhubao.manage.module.organization.mapper.StoreMapper;
import com.zhubao.manage.module.task.dto.TaskQueryDTO;
import com.zhubao.manage.module.user.entity.User;
import com.zhubao.manage.module.user.mapper.UserMapper;
import com.zhubao.manage.module.task.entity.TaskInstance;
import com.zhubao.manage.module.task.entity.TaskTemplate;
import com.zhubao.manage.module.task.mapper.TaskInstanceMapper;
import com.zhubao.manage.module.task.mapper.TaskTemplateMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {

    private final TaskInstanceMapper taskInstanceMapper;
    private final TaskTemplateMapper taskTemplateMapper;
    private final StoreMapper storeMapper;
    private final UserMapper userMapper;
    private final com.zhubao.manage.common.interceptor.UserContextHolder userContextHolder;

    public TaskService(TaskInstanceMapper taskInstanceMapper,
                       TaskTemplateMapper taskTemplateMapper,
                       StoreMapper storeMapper, UserMapper userMapper,
                       com.zhubao.manage.common.interceptor.UserContextHolder userContextHolder) {
        this.taskInstanceMapper = taskInstanceMapper;
        this.taskTemplateMapper = taskTemplateMapper;
        this.storeMapper = storeMapper;
        this.userMapper = userMapper;
        this.userContextHolder = userContextHolder;
    }

    // ============ 生成任务 ============

    /**
     * 根据模板批量生成任务实例到指定门店
     */
    @Transactional
    public List<TaskInstance> generateTasks(Long templateId, List<Long> storeIds) {
        TaskTemplate template = taskTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ErrorCode.TASK_TEMPLATE_NOT_FOUND);
        }

        // 批量查门店 (P1-6)
        List<Store> stores = storeMapper.selectBatchIds(storeIds);
        java.util.Map<Long, Store> storeMap = stores.stream().collect(java.util.stream.Collectors.toMap(Store::getId, s -> s));

        List<TaskInstance> instances = new ArrayList<>();
        for (Long storeId : storeIds) {
            Store store = storeMap.get(storeId);

            TaskInstance instance = new TaskInstance();
            instance.setTemplateId(templateId);
            instance.setTaskTitle(template.getTemplateName());
            instance.setDimension(template.getDimension());
            instance.setCategory(template.getCategory());
            instance.setStoreId(storeId);
            instance.setAssigneeId(store != null ? store.getStoreManagerId() : null);
            instance.setAuditorId(null);
            instance.setStartTime(LocalDateTime.now());
            instance.setDueTime(calcDueTime(template.getDueTimeRule()));
            instance.setStatus("PENDING");
            instance.setPriority("MEDIUM");
            instance.setSourceType(template.getIsForce() == 1 ? "HQ" : "CYCLE");
            instance.setIsOverdue(0);
            instance.setOverdueMinutes(0);
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            instance.setTaskNo("TK" + date + String.format("%06d", System.currentTimeMillis() % 1000000 + instances.size()));
            taskInstanceMapper.insert(instance);
            instances.add(instance);
        }
        return instances;
    }

    /** 单个创建任务（手动创建） */
    @Transactional
    public TaskInstance createTask(TaskInstance task) {
        if (task.getTaskTitle() == null || task.getTaskTitle().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "任务标题不能为空");
        }
        // assigneeId 兜底：未指定负责人时默认为当前登录用户
        if (task.getAssigneeId() == null) {
            task.setAssigneeId(userContextHolder.getUserId());
        }
        // storeId 兜底：从负责人推断，仍为空则取第一条门店
        if (task.getStoreId() == null) {
            User assignee = userMapper.selectById(task.getAssigneeId());
            if (assignee != null && assignee.getStoreId() != null) {
                task.setStoreId(assignee.getStoreId());
            }
            if (task.getStoreId() == null) {
                List<Store> stores = storeMapper.selectList(null);
                if (stores != null && !stores.isEmpty()) {
                    task.setStoreId(stores.get(0).getId());
                }
            }
        }
        task.setId(null);
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        task.setTaskNo("TK" + date + String.format("%06d", System.currentTimeMillis() % 1000000));
        task.setStatus(task.getStatus() == null ? "PENDING" : task.getStatus());
        task.setPriority(task.getPriority() == null ? "MEDIUM" : task.getPriority());
        task.setDimension(task.getDimension() == null ? "COMPREHENSIVE" : task.getDimension());
        task.setCategory(task.getCategory() == null ? "" : task.getCategory());
        task.setSourceType(task.getSourceType() == null ? "MANUAL" : task.getSourceType());
        task.setStartTime(task.getStartTime() == null ? LocalDateTime.now() : task.getStartTime());
        if (task.getDueTime() == null) {
            task.setDueTime(task.getStartTime().plusDays(3));
        }
        task.setIsOverdue(0);
        task.setOverdueMinutes(0);
        taskInstanceMapper.insert(task);
        return task;
    }

    // ============ 查询 ============

    /** 多条件筛选分页 */
    public IPage<TaskInstance> page(TaskQueryDTO query) {
        Page<TaskInstance> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<TaskInstance> w = buildQueryWrapper(query);
        w.orderByDesc(TaskInstance::getCreatedAt);
        IPage<TaskInstance> result = taskInstanceMapper.selectPage(page, w);
        fillAssigneeNames(result.getRecords());
        return result;
    }

    /** 多条件筛选列表 */
    public List<TaskInstance> listTasks(TaskQueryDTO query) {
        List<TaskInstance> list = taskInstanceMapper.selectList(buildQueryWrapper(query)
                .orderByDesc(TaskInstance::getCreatedAt));
        fillAssigneeNames(list);
        return list;
    }

    /** 我的任务 */
    public List<TaskInstance> getMyTasks(Long userId, String status) {
        LambdaQueryWrapper<TaskInstance> w = new LambdaQueryWrapper<TaskInstance>()
                .eq(TaskInstance::getAssigneeId, userId);
        if (StringUtils.isNotBlank(status)) {
            w.eq(TaskInstance::getStatus, status);
        }
        w.orderByDesc(TaskInstance::getCreatedAt);
        List<TaskInstance> list = taskInstanceMapper.selectList(w);
        fillAssigneeNames(list);
        return list;
    }

    /** 批量填充 assigneeName / auditorName */
    private void fillAssigneeNames(List<TaskInstance> tasks) {
        if (tasks.isEmpty()) return;
        java.util.Set<Long> userIds = new java.util.HashSet<>();
        for (TaskInstance t : tasks) {
            if (t.getAssigneeId() != null) userIds.add(t.getAssigneeId());
            if (t.getAuditorId() != null) userIds.add(t.getAuditorId());
        }
        if (userIds.isEmpty()) return;
        java.util.Map<Long, String> nameMap = new java.util.HashMap<>();
        for (User u : userMapper.selectBatchIds(userIds)) {
            nameMap.put(u.getId(), u.getRealName());
        }
        for (TaskInstance t : tasks) {
            t.setAssigneeName(nameMap.getOrDefault(t.getAssigneeId(), null));
            t.setAuditorName(nameMap.getOrDefault(t.getAuditorId(), null));
        }
    }

    /** 待审核任务 */
    public List<TaskInstance> getMyAuditTasks(Long auditorId) {
        return taskInstanceMapper.selectList(
                new LambdaQueryWrapper<TaskInstance>()
                        .eq(TaskInstance::getAuditorId, auditorId)
                        .eq(TaskInstance::getStatus, "SUBMITTED")
                        .orderByAsc(TaskInstance::getDueTime));
    }

    /** 任务详情 */
    public TaskInstance detail(Long id) {
        return getById(id);
    }

    // ============ 操作 ============

    /** 取消任务 */
    @Transactional
    public void cancelTask(Long taskId) {
        TaskInstance task = getById(taskId);
        TaskStatusEnum status = TaskStatusEnum.valueOf(task.getStatus());
        if (!status.canCancel()) {
            throw new BusinessException(ErrorCode.TASK_STATUS_INVALID.getCode(),
                    "当前状态[" + task.getStatus() + "]不允许取消");
        }
        task.setStatus(TaskStatusEnum.CANCELLED.getCode());
        taskInstanceMapper.updateById(task);
    }

    /** 作废任务（管理员强制） */
    @Transactional
    public void voidTask(Long taskId) {
        TaskInstance task = getById(taskId);
        TaskStatusEnum status = TaskStatusEnum.valueOf(task.getStatus());
        if (!status.canVoid()) {
            throw new BusinessException(ErrorCode.TASK_STATUS_INVALID.getCode(),
                    "当前状态[" + task.getStatus() + "]不允许作废");
        }
        task.setStatus(TaskStatusEnum.VOIDED.getCode());
        taskInstanceMapper.updateById(task);
    }

    /** 开始执行 */
    @Transactional
    public void startTask(Long taskId) {
        TaskInstance task = getById(taskId);
        if (!"PENDING".equals(task.getStatus()) && !"READY".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.TASK_STATUS_INVALID);
        }
        task.setStatus(TaskStatusEnum.IN_PROGRESS.getCode());
        taskInstanceMapper.updateById(task);
    }

    /** 通用更新任务 */
    @Transactional
    public void updateTask(TaskInstance task) {
        TaskInstance exist = getById(task.getId());
        if (exist == null) throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        taskInstanceMapper.updateById(task);
    }

    // ---- 内部 ----

    private LambdaQueryWrapper<TaskInstance> buildQueryWrapper(TaskQueryDTO q) {
        LambdaQueryWrapper<TaskInstance> w = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(q.getStatus())) w.eq(TaskInstance::getStatus, q.getStatus());
        if (q.getStoreId() != null) w.eq(TaskInstance::getStoreId, q.getStoreId());
        if (q.getAssigneeId() != null) w.eq(TaskInstance::getAssigneeId, q.getAssigneeId());
        if (q.getAuditorId() != null) w.eq(TaskInstance::getAuditorId, q.getAuditorId());
        if (StringUtils.isNotBlank(q.getDimension())) w.eq(TaskInstance::getDimension, q.getDimension());
        if (StringUtils.isNotBlank(q.getCategory())) w.eq(TaskInstance::getCategory, q.getCategory());
        if (StringUtils.isNotBlank(q.getSourceType())) w.eq(TaskInstance::getSourceType, q.getSourceType());
        if (StringUtils.isNotBlank(q.getPriority())) w.eq(TaskInstance::getPriority, q.getPriority());
        if (q.getIsOverdue() != null) w.eq(TaskInstance::getIsOverdue, q.getIsOverdue());
        if (StringUtils.isNotBlank(q.getKeyword())) {
            w.and(w2 -> w2.like(TaskInstance::getTaskTitle, q.getKeyword())
                    .or().like(TaskInstance::getTaskNo, q.getKeyword()));
        }
        if (StringUtils.isNotBlank(q.getStartDate())) {
            w.ge(TaskInstance::getCreatedAt, q.getStartDate() + " 00:00:00");
        }
        if (StringUtils.isNotBlank(q.getEndDate())) {
            w.le(TaskInstance::getCreatedAt, q.getEndDate() + " 23:59:59");
        }
        return w;
    }

    private TaskInstance getById(Long id) {
        TaskInstance t = taskInstanceMapper.selectById(id);
        if (t == null) throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        return t;
    }

    private LocalDateTime calcDueTime(String dueTimeRule) {
        if (StringUtils.isBlank(dueTimeRule)) return LocalDateTime.now().plusDays(1);
        try {
            if (dueTimeRule.contains("当日")) {
                String time = dueTimeRule.replace("当日", "").trim();
                String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return LocalDateTime.parse(today + "T" + time + ":00");
            }
        } catch (Exception ignored) {}
        return LocalDateTime.now().plusDays(1);
    }
}
