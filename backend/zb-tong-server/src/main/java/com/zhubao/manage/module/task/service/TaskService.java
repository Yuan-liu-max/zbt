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

    public TaskService(TaskInstanceMapper taskInstanceMapper,
                       TaskTemplateMapper taskTemplateMapper,
                       StoreMapper storeMapper) {
        this.taskInstanceMapper = taskInstanceMapper;
        this.taskTemplateMapper = taskTemplateMapper;
        this.storeMapper = storeMapper;
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

        List<TaskInstance> instances = new ArrayList<>();
        for (Long storeId : storeIds) {
            Store store = storeMapper.selectById(storeId);

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
            taskInstanceMapper.insert(instance);
            // 基于 DB 自增 ID 生成任务编号，多实例安全
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            instance.setTaskNo("TK" + date + String.format("%06d", instance.getId() % 1000000));
            taskInstanceMapper.updateById(instance);
            instances.add(instance);
        }
        return instances;
    }

    // ============ 查询 ============

    /** 多条件筛选分页 */
    public IPage<TaskInstance> page(TaskQueryDTO query) {
        Page<TaskInstance> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<TaskInstance> w = buildQueryWrapper(query);
        w.orderByDesc(TaskInstance::getCreatedAt);
        return taskInstanceMapper.selectPage(page, w);
    }

    /** 多条件筛选列表 */
    public List<TaskInstance> listTasks(TaskQueryDTO query) {
        return taskInstanceMapper.selectList(buildQueryWrapper(query)
                .orderByDesc(TaskInstance::getCreatedAt));
    }

    /** 我的任务 */
    public List<TaskInstance> getMyTasks(Long userId, String status) {
        LambdaQueryWrapper<TaskInstance> w = new LambdaQueryWrapper<TaskInstance>()
                .eq(TaskInstance::getAssigneeId, userId);
        if (StringUtils.isNotBlank(status)) {
            w.eq(TaskInstance::getStatus, status);
        }
        w.orderByDesc(TaskInstance::getCreatedAt);
        return taskInstanceMapper.selectList(w);
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
