package com.zhubao.manage.module.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.module.task.entity.TaskInstance;
import com.zhubao.manage.module.task.entity.TaskSubmission;
import com.zhubao.manage.module.task.mapper.TaskInstanceMapper;
import com.zhubao.manage.module.task.mapper.TaskSubmissionMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI任务执行质量评分引擎
 *
 * 100分 = 按时完成(20) + 文字描述质量(25) + 图片质量(20) + 表单完整度(20) + 内容合理性(15)
 */
@Service
public class AIScoreService {

    private final TaskInstanceMapper taskInstanceMapper;
    private final TaskSubmissionMapper submissionMapper;

    public AIScoreService(TaskInstanceMapper tim, TaskSubmissionMapper sm) {
        this.taskInstanceMapper = tim; this.submissionMapper = sm;
    }

    /**
     * 对任务执行质量进行AI评分
     */
    public Map<String, Object> scoreTask(Long taskId) {
        TaskInstance task = taskInstanceMapper.selectById(taskId);
        if (task == null) return errorResult("任务不存在");

        TaskSubmission submission = submissionMapper.selectOne(
                new LambdaQueryWrapper<TaskSubmission>()
                        .eq(TaskSubmission::getTaskId, taskId)
                        .orderByDesc(TaskSubmission::getCreatedAt)
                        .last("LIMIT 1"));

        // ---- 1. 按时完成 (20分) ----
        BigDecimal onTimeScore = BigDecimal.valueOf(20);
        if (task.getCompletedTime() != null && task.getDueTime() != null
                && task.getCompletedTime().isAfter(task.getDueTime())) {
            onTimeScore = BigDecimal.valueOf(Math.max(0, 20 - task.getOverdueMinutes() * 0.1));
        }
        if (task.getIsOverdue() != null && task.getIsOverdue() == 1) {
            onTimeScore = BigDecimal.ZERO;
        }

        // ---- 2. 文字描述质量 (25分) ----
        BigDecimal textScore = BigDecimal.valueOf(15); // 默认15
        if (submission != null && submission.getTextContent() != null) {
            int textLen = submission.getTextContent().length();
            if (textLen > 200) textScore = BigDecimal.valueOf(25);
            else if (textLen > 100) textScore = BigDecimal.valueOf(22);
            else if (textLen > 50) textScore = BigDecimal.valueOf(20);
            else if (textLen > 20) textScore = BigDecimal.valueOf(18);
        }

        // ---- 3. 图片质量 (20分) ----
        BigDecimal photoScore = BigDecimal.valueOf(10); // 默认10
        if (submission != null && submission.getPhotoUrls() != null) {
            int photoCount = submission.getPhotoUrls().split(",").length;
            if (photoCount >= 3) photoScore = BigDecimal.valueOf(20);
            else if (photoCount >= 2) photoScore = BigDecimal.valueOf(17);
            else if (photoCount >= 1) photoScore = BigDecimal.valueOf(14);
        }

        // ---- 4. 表单完整度 (20分) ----
        BigDecimal formScore = BigDecimal.valueOf(10);
        if (submission != null && submission.getFormData() != null
                && submission.getFormData().length() > 20) {
            formScore = BigDecimal.valueOf(20);
        }

        // ---- 5. 内容合理性 (15分) ----
        // TODO: 接入AI判断内容合理性
        BigDecimal reasonScore = BigDecimal.valueOf(12);

        BigDecimal total = onTimeScore.add(textScore).add(photoScore).add(formScore).add(reasonScore)
                .setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskId", taskId);
        result.put("totalScore", total);
        result.put("onTimeScore", onTimeScore);
        result.put("textScore", textScore);
        result.put("photoScore", photoScore);
        result.put("formScore", formScore);
        result.put("reasonScore", reasonScore);
        return result;
    }

    private Map<String, Object> errorResult(String msg) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", msg); return m;
    }
}
