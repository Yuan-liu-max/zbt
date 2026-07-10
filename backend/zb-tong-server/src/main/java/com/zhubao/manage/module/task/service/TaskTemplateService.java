package com.zhubao.manage.module.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.actiontemplate.entity.ActionTemplate;
import com.zhubao.manage.module.actiontemplate.mapper.ActionTemplateMapper;
import com.zhubao.manage.module.task.entity.TaskTemplate;
import com.zhubao.manage.module.task.mapper.TaskTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskTemplateService {

    private final TaskTemplateMapper taskTemplateMapper;
    private final ActionTemplateMapper actionTemplateMapper;
    private final UserContextHolder userContextHolder;

    public TaskTemplateService(TaskTemplateMapper taskTemplateMapper,
                               ActionTemplateMapper actionTemplateMapper,
                               UserContextHolder userContextHolder) {
        this.taskTemplateMapper = taskTemplateMapper;
        this.actionTemplateMapper = actionTemplateMapper;
        this.userContextHolder = userContextHolder;
    }

    public List<TaskTemplate> listAll() {
        return taskTemplateMapper.selectList(
                new LambdaQueryWrapper<TaskTemplate>().orderByDesc(TaskTemplate::getCreatedAt));
    }

    public TaskTemplate detail(Long id) {
        TaskTemplate tt = getById(id);
        // 补充关联的 ActionTemplate 信息
        if (tt.getActionId() != null) {
            ActionTemplate at = actionTemplateMapper.selectById(tt.getActionId());
            // 这里可扩展VO返回动作名称
        }
        return tt;
    }

    @Transactional
    public TaskTemplate create(TaskTemplate entity) {
        entity.setCreatedBy(userContextHolder.getUserId());
        taskTemplateMapper.insert(entity);
        return entity;
    }

    @Transactional
    public TaskTemplate update(Long id, TaskTemplate entity) {
        getById(id);
        entity.setId(id);
        taskTemplateMapper.updateById(entity);
        return detail(id);
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        taskTemplateMapper.deleteById(id);
    }

    @Transactional
    public void toggleStatus(Long id) {
        TaskTemplate tt = getById(id);
        tt.setStatus("ENABLED".equals(tt.getStatus()) ? "DISABLED" : "ENABLED");
        taskTemplateMapper.updateById(tt);
    }

    /** 根据动作ID查询关联模板 */
    public List<TaskTemplate> listByActionId(Long actionId) {
        return taskTemplateMapper.selectList(
                new LambdaQueryWrapper<TaskTemplate>().eq(TaskTemplate::getActionId, actionId));
    }

    private TaskTemplate getById(Long id) {
        TaskTemplate tt = taskTemplateMapper.selectById(id);
        if (tt == null) throw new BusinessException(ErrorCode.TASK_TEMPLATE_NOT_FOUND);
        return tt;
    }
}
