package com.zhubao.manage.module.actiontemplate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.actiontemplate.entity.ActionTemplate;
import com.zhubao.manage.module.actiontemplate.mapper.ActionTemplateMapper;
import com.zhubao.manage.module.organization.entity.Store;
import com.zhubao.manage.module.organization.mapper.StoreMapper;
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
public class ActionTemplateService {

    private final ActionTemplateMapper actionTemplateMapper;
    private final TaskTemplateMapper taskTemplateMapper;
    private final TaskInstanceMapper taskInstanceMapper;
    private final StoreMapper storeMapper;
    private final UserContextHolder userContextHolder;

    private static final java.util.concurrent.atomic.AtomicInteger taskNoSeq = new java.util.concurrent.atomic.AtomicInteger(0);

    public ActionTemplateService(ActionTemplateMapper actionTemplateMapper,
                                 TaskTemplateMapper taskTemplateMapper,
                                 TaskInstanceMapper taskInstanceMapper,
                                 StoreMapper storeMapper,
                                 UserContextHolder userContextHolder) {
        this.actionTemplateMapper = actionTemplateMapper;
        this.taskTemplateMapper = taskTemplateMapper;
        this.taskInstanceMapper = taskInstanceMapper;
        this.storeMapper = storeMapper;
        this.userContextHolder = userContextHolder;
    }

    // ============ CRUD ============

    public List<ActionTemplate> listAll() {
        return actionTemplateMapper.selectList(
                new LambdaQueryWrapper<ActionTemplate>().orderByDesc(ActionTemplate::getCreatedAt));
    }

    public ActionTemplate detail(Long id) {
        return getById(id);
    }

    @Transactional
    public ActionTemplate create(ActionTemplate entity) {
        entity.setCreatedBy(userContextHolder.getUserId());
        actionTemplateMapper.insert(entity);
        return entity;
    }

    @Transactional
    public ActionTemplate update(Long id, ActionTemplate entity) {
        getById(id);
        entity.setId(id);
        actionTemplateMapper.updateById(entity);
        return detail(id);
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        actionTemplateMapper.deleteById(id);
    }

    /** 启停 */
    @Transactional
    public void toggleStatus(Long id) {
        ActionTemplate at = getById(id);
        at.setStatus("ENABLED".equals(at.getStatus()) ? "DISABLED" : "ENABLED");
        actionTemplateMapper.updateById(at);
    }

    // ============ 下发到门店 ============

    /**
     * 根据 ActionTemplate 一键下发到指定门店：
     * 1. 为每个门店创建/获取 TaskTemplate
     * 2. 立即生成 task_instance
     */
    @Transactional
    public List<TaskInstance> dispatchToStores(Long actionId, List<Long> storeIds) {
        ActionTemplate action = getById(actionId);

        List<TaskInstance> instances = new ArrayList<>();
        for (Long storeId : storeIds) {
            // 1. 查找或创建 TaskTemplate
            TaskTemplate template = findOrCreateTemplate(action, storeId);

            // 2. 生成任务实例
            TaskInstance instance = generateInstance(template, storeId);
            instances.add(instance);
        }
        return instances;
    }

    private TaskTemplate findOrCreateTemplate(ActionTemplate action, Long storeId) {
        // 查找是否已存在由该动作生成的模板
        TaskTemplate existing = taskTemplateMapper.selectOne(
                new LambdaQueryWrapper<TaskTemplate>()
                        .eq(TaskTemplate::getActionId, action.getId())
                        .like(TaskTemplate::getApplicableStoreIds, String.valueOf(storeId))
                        .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }

        // 创建新模板
        TaskTemplate tmpl = new TaskTemplate();
        tmpl.setTemplateName(action.getActionName());
        tmpl.setActionId(action.getId());
        tmpl.setDimension(action.getDimension());
        tmpl.setCategory(action.getCategory());
        tmpl.setDescription(action.getDescription());
        tmpl.setExecutionStandard(action.getExecutionStandard());
        tmpl.setRequiredPhotos(action.getRequiredPhotos());
        tmpl.setRequiredText(action.getRequiredText());
        tmpl.setRequiredForm(action.getRequiredForm());
        tmpl.setFormSchemaId(action.getFormSchemaId());
        tmpl.setRequireAudit(action.getRequireAudit());
        tmpl.setDefaultAuditorRole(action.getDefaultAuditorRole());
        tmpl.setFrequencyType(action.getFrequencyType());
        tmpl.setCronExpression(action.getCronExpression());
        tmpl.setDueTimeRule(action.getDueTimeRule());
        tmpl.setScoreWeight(action.getScoreWeight());
        tmpl.setIsDefault(0);
        tmpl.setIsForce(action.getIsForce());
        tmpl.setApplicableStoreIds(String.valueOf(storeId));
        tmpl.setStatus("ENABLED");
        tmpl.setCreatedBy(userContextHolder.getUserId());
        taskTemplateMapper.insert(tmpl);
        return tmpl;
    }

    private TaskInstance generateInstance(TaskTemplate template, Long storeId) {
        Store store = storeMapper.selectById(storeId);

        TaskInstance instance = new TaskInstance();
        instance.setTaskNo(generateTaskNo());
        instance.setTemplateId(template.getId());
        instance.setTaskTitle(template.getTemplateName());
        instance.setDimension(template.getDimension());
        instance.setCategory(template.getCategory());
        instance.setStoreId(storeId);
        instance.setAssigneeId(store != null ? store.getStoreManagerId() : null);
        instance.setStartTime(LocalDateTime.now());
        instance.setDueTime(calcDueTime(template.getDueTimeRule()));
        instance.setStatus("PENDING");
        instance.setPriority("MEDIUM");
        instance.setSourceType(template.getIsForce() == 1 ? "HQ" : "MANUAL");
        instance.setIsOverdue(0);
        instance.setOverdueMinutes(0);
        taskInstanceMapper.insert(instance);
        return instance;
    }

    private String generateTaskNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "TK" + date + String.format("%06d", taskNoSeq.incrementAndGet() % 1000000);
    }

    private LocalDateTime calcDueTime(String dueTimeRule) {
        if (StringUtils.isBlank(dueTimeRule)) {
            return LocalDateTime.now().plusDays(1);
        }
        try {
            // 简化处理：截止规则如 "当日 10:00" 解析为今天10:00
            if (dueTimeRule.contains("当日")) {
                String time = dueTimeRule.replace("当日", "").trim();
                String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return LocalDateTime.parse(today + "T" + time + ":00");
            }
        } catch (Exception ignored) {}
        return LocalDateTime.now().plusDays(1);
    }

    private ActionTemplate getById(Long id) {
        ActionTemplate at = actionTemplateMapper.selectById(id);
        if (at == null) throw new BusinessException(ErrorCode.ACTION_TEMPLATE_NOT_FOUND);
        return at;
    }
}
