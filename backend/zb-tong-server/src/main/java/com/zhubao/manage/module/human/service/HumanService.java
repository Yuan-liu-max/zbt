package com.zhubao.manage.module.human.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.module.human.entity.*;
import com.zhubao.manage.module.human.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HumanService {

    // ---- 7 mappers ----
    private final EmployeeProfileMapper profileMapper;
    private final EmployeeInterviewMapper interviewMapper;
    private final EmployeeAssessmentMapper assessmentMapper;
    private final EmployeeTrainingMapper trainingMapper;
    private final EmployeeTrainingRecordMapper trainingRecordMapper;
    private final EmployeeMonthlyReviewMapper monthlyReviewMapper;
    private final EmployeeLevelRecordMapper levelRecordMapper;

    public HumanService(EmployeeProfileMapper p, EmployeeInterviewMapper i,
                        EmployeeAssessmentMapper a, EmployeeTrainingMapper t,
                        EmployeeTrainingRecordMapper tr, EmployeeMonthlyReviewMapper mr,
                        EmployeeLevelRecordMapper lr) {
        this.profileMapper = p; this.interviewMapper = i; this.assessmentMapper = a;
        this.trainingMapper = t; this.trainingRecordMapper = tr;
        this.monthlyReviewMapper = mr; this.levelRecordMapper = lr;
    }

    // ==================== 通用 CRUD ====================

    public <T> List<T> all(BaseMapper<T> m, LambdaQueryWrapper<T> w) { return m.selectList(w); }
    public <T> IPage<T> page(BaseMapper<T> m, PageDTO page, LambdaQueryWrapper<T> w) {
        return m.selectPage(new Page<>(page.getPageNum(), page.getPageSize()), w);
    }
    public <T> T get(BaseMapper<T> m, Long id, String entityName) {
        T t = m.selectById(id); if (t == null) throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), entityName + "不存在"); return t;
    }
    public <T> void save(BaseMapper<T> m, T t) { m.insert(t); }
    public <T> void update(BaseMapper<T> m, T t) { m.updateById(t); }
    public <T> void del(BaseMapper<T> m, Long id) { m.deleteById(id); }

    // ==================== 聚合员工档案 ====================

    /**
     * 员工完整档案：
     *   基本 + 面谈(近6条) + 考核(近6条) + 培训记录 + 月度复盘 + 分层历史 + TODO: AI画像
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEmployeeProfile(Long userId) {
        EmployeeProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<EmployeeProfile>().eq(EmployeeProfile::getUserId, userId));
        if (profile == null) throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "员工档案不存在");

        List<EmployeeInterview> interviews = interviewMapper.selectList(
                new LambdaQueryWrapper<EmployeeInterview>().eq(EmployeeInterview::getEmployeeId, userId)
                        .orderByDesc(EmployeeInterview::getInterviewDate).last("LIMIT 6"));

        List<EmployeeAssessment> assessments = assessmentMapper.selectList(
                new LambdaQueryWrapper<EmployeeAssessment>().eq(EmployeeAssessment::getEmployeeId, userId)
                        .orderByDesc(EmployeeAssessment::getAssessmentWeek).last("LIMIT 6"));

        // 查该员工的培训参与记录 → 关联培训主题 (P2-17 fix)
        List<Long> trainingIds = trainingRecordMapper.selectList(
                new LambdaQueryWrapper<EmployeeTrainingRecord>().eq(EmployeeTrainingRecord::getEmployeeId, userId))
                .stream().map(EmployeeTrainingRecord::getTrainingId).distinct().collect(Collectors.toList());
        List<EmployeeTraining> trainings = trainingIds.isEmpty() ? new ArrayList<>()
                : trainingMapper.selectBatchIds(trainingIds).stream()
                .sorted((a, b) -> b.getTrainingDate().compareTo(a.getTrainingDate()))
                .limit(10).collect(Collectors.toList());

        List<EmployeeMonthlyReview> reviews = monthlyReviewMapper.selectList(
                new LambdaQueryWrapper<EmployeeMonthlyReview>().eq(EmployeeMonthlyReview::getEmployeeId, userId)
                        .orderByDesc(EmployeeMonthlyReview::getReviewMonth).last("LIMIT 6"));

        List<EmployeeLevelRecord> levelHistory = levelRecordMapper.selectList(
                new LambdaQueryWrapper<EmployeeLevelRecord>().eq(EmployeeLevelRecord::getEmployeeId, userId)
                        .orderByDesc(EmployeeLevelRecord::getEvalMonth));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("profile", profile);
        result.put("recentInterviews", interviews);
        result.put("recentAssessments", assessments);
        result.put("trainings", trainings);
        result.put("monthlyReviews", reviews);
        result.put("levelHistory", levelHistory);
        result.put("aiProfile", null); // TODO: 接入AI画像

        return result;
    }
}
