package com.zhubao.manage.module.human.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.annotation.OperateLog;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.human.entity.*;
import com.zhubao.manage.module.human.mapper.*;
import com.zhubao.manage.module.human.service.HumanService;
import com.zhubao.manage.module.user.entity.User;
import com.zhubao.manage.module.user.mapper.UserMapper;
import javax.validation.Valid;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Api(tags = "人效管理")
@RestController
@RequestMapping("/human")
@org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
public class HumanController {

    private final HumanService svc;
    private final EmployeeProfileMapper profileMapper;
    private final EmployeeInterviewMapper interviewMapper;
    private final EmployeeAssessmentMapper assessmentMapper;
    private final EmployeeTrainingMapper trainingMapper;
    private final EmployeeTrainingRecordMapper trainingRecordMapper;
    private final EmployeeMonthlyReviewMapper monthlyReviewMapper;
    private final EmployeeLevelRecordMapper levelRecordMapper;
    private final MeetingMapper meetingMapper;
    private final UserMapper userMapper;

    public HumanController(HumanService svc, EmployeeProfileMapper p, EmployeeInterviewMapper i,
                           EmployeeAssessmentMapper a, EmployeeTrainingMapper t,
                           EmployeeTrainingRecordMapper tr, EmployeeMonthlyReviewMapper mr,
                           EmployeeLevelRecordMapper lr, MeetingMapper mm, UserMapper um) {
        this.svc = svc; this.profileMapper = p; this.interviewMapper = i;
        this.assessmentMapper = a; this.trainingMapper = t; this.trainingRecordMapper = tr;
        this.monthlyReviewMapper = mr; this.levelRecordMapper = lr; this.meetingMapper = mm;
        this.userMapper = um;
    }

    /** 批量填充 employeeName / assessorName（transient 字段，不落库） */
    private void fillAssessmentNames(List<EmployeeAssessment> list) {
        if (list == null || list.isEmpty()) return;
        Set<Long> userIds = new HashSet<>();
        for (EmployeeAssessment e : list) {
            if (e.getEmployeeId() != null) userIds.add(e.getEmployeeId());
            if (e.getAssessorId() != null) userIds.add(e.getAssessorId());
        }
        if (userIds.isEmpty()) return;
        Map<Long, String> nameMap = new HashMap<>();
        for (User u : userMapper.selectBatchIds(userIds)) {
            nameMap.put(u.getId(), StringUtils.isNotBlank(u.getRealName()) ? u.getRealName() : u.getUsername());
        }
        for (EmployeeAssessment e : list) {
            e.setEmployeeName(nameMap.get(e.getEmployeeId()));
            e.setAssessorName(nameMap.get(e.getAssessorId()));
        }
    }

    // ===== 聚合档案 =====
    @ApiOperation("员工完整档案")
    @GetMapping("/employees/{userId}/profile")
    public ApiResult<Map<String, Object>> profile(@PathVariable Long userId) {
        return ApiResult.ok(svc.getEmployeeProfile(userId));
    }

    // ===== 晨会/夕会 (EmployeeProfile 管理) =====
    @ApiOperation("员工档案列表") @GetMapping("/employees")
    public ApiResult<List<EmployeeProfile>> listEmployees() { return ApiResult.ok(svc.all(profileMapper, new LambdaQueryWrapper<EmployeeProfile>())); }
    @OperateLog(module = "人效管理", action = "CREATE", targetType = "EMPLOYEE")
    @ApiOperation("新增员工档案") @PostMapping("/employees")
    public ApiResult<Void> createEmployee(@Valid @RequestBody EmployeeProfile e) { svc.save(profileMapper, e); return ApiResult.ok(); }
    @OperateLog(module = "人效管理", action = "UPDATE", targetType = "EMPLOYEE", targetIdExpr = "#id")
    @ApiOperation("更新员工档案") @PutMapping("/employees/{id}")
    public ApiResult<Void> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeProfile e) { e.setId(id); svc.update(profileMapper, e); return ApiResult.ok(); }

    // ===== 面谈 =====
    @ApiOperation("面谈列表（分页+筛选）") @GetMapping("/interviews")
    public ApiResult<PageResult<EmployeeInterview>> listInterviews(@Valid PageDTO dto,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<EmployeeInterview> w = new LambdaQueryWrapper<EmployeeInterview>();
        if (StringUtils.isNotBlank(keyword)) w.like(EmployeeInterview::getMainProblem, keyword);
        if (StringUtils.isNotBlank(status)) w.eq(EmployeeInterview::getMindsetStatus, status);
        w.orderByDesc(EmployeeInterview::getCreatedAt);
        IPage<EmployeeInterview> r = svc.page(interviewMapper, dto, w);
        return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("分页面谈") @GetMapping("/interviews/page")
    public ApiResult<PageResult<EmployeeInterview>> pageInterviews(@Valid PageDTO dto,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<EmployeeInterview> w = new LambdaQueryWrapper<EmployeeInterview>();
        if (StringUtils.isNotBlank(keyword)) w.like(EmployeeInterview::getMainProblem, keyword);
        if (StringUtils.isNotBlank(status)) w.eq(EmployeeInterview::getMindsetStatus, status);
        IPage<EmployeeInterview> r = svc.page(interviewMapper, dto, w);
        return ApiResult.ok(PageResult.of(r)); }
    @OperateLog(module = "人效管理", action = "CREATE", targetType = "INTERVIEW")
    @ApiOperation("新增面谈") @PostMapping("/interviews")
    public ApiResult<Void> createInterview(@Valid @RequestBody EmployeeInterview e) { svc.save(interviewMapper, e); return ApiResult.ok(); }
    @OperateLog(module = "人效管理", action = "UPDATE", targetType = "INTERVIEW", targetIdExpr = "#id")
    @ApiOperation("更新面谈") @PutMapping("/interviews/{id}")
    public ApiResult<Void> updateInterview(@PathVariable Long id, @Valid @RequestBody EmployeeInterview e) { e.setId(id); svc.update(interviewMapper, e); return ApiResult.ok(); }
    @ApiOperation("面谈详情") @GetMapping("/interviews/{id}")
    public ApiResult<EmployeeInterview> getInterview(@PathVariable Long id) { return ApiResult.ok(svc.get(interviewMapper, id, "面谈")); }
    @OperateLog(module = "人效管理", action = "DELETE", targetType = "INTERVIEW", targetIdExpr = "#id")
    @ApiOperation("删除面谈") @DeleteMapping("/interviews/{id}")
    public ApiResult<Void> deleteInterview(@PathVariable Long id) { svc.del(interviewMapper, id); return ApiResult.ok(); }

    // ===== 考核 =====
    @ApiOperation("考核列表（分页+筛选）") @GetMapping("/assessments")
    public ApiResult<PageResult<EmployeeAssessment>> listAssessments(@Valid PageDTO dto,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String assessmentWeek,
            @RequestParam(required = false) Long assessorId,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<EmployeeAssessment> w = new LambdaQueryWrapper<EmployeeAssessment>();
        if (StringUtils.isNotBlank(keyword)) w.like(EmployeeAssessment::getImprovementAdvice, keyword);
        if (StringUtils.isNotBlank(assessmentWeek)) w.like(EmployeeAssessment::getAssessmentWeek, assessmentWeek);
        if (assessorId != null) w.eq(EmployeeAssessment::getAssessorId, assessorId);
        w.orderByDesc(EmployeeAssessment::getCreatedAt);
        IPage<EmployeeAssessment> r = svc.page(assessmentMapper, dto, w);
        fillAssessmentNames(r.getRecords());
        return ApiResult.ok(PageResult.of(r)); }
    @OperateLog(module = "人效管理", action = "CREATE", targetType = "ASSESSMENT")
    @ApiOperation("新增考核") @PostMapping("/assessments")
    public ApiResult<Void> createAssessment(@Valid @RequestBody EmployeeAssessment e) { svc.save(assessmentMapper, e); return ApiResult.ok(); }
    @OperateLog(module = "人效管理", action = "UPDATE", targetType = "ASSESSMENT", targetIdExpr = "#id")
    @ApiOperation("更新考核") @PutMapping("/assessments/{id}")
    public ApiResult<Void> updateAssessment(@PathVariable Long id, @Valid @RequestBody EmployeeAssessment e) { e.setId(id); svc.update(assessmentMapper, e); return ApiResult.ok(); }
    @ApiOperation("考核详情") @GetMapping("/assessments/{id}")
    public ApiResult<EmployeeAssessment> getAssessment(@PathVariable Long id) {
        EmployeeAssessment e = svc.get(assessmentMapper, id, "考核");
        fillAssessmentNames(java.util.Collections.singletonList(e));
        return ApiResult.ok(e); }
    @OperateLog(module = "人效管理", action = "DELETE", targetType = "ASSESSMENT", targetIdExpr = "#id")
    @ApiOperation("删除考核") @DeleteMapping("/assessments/{id}")
    public ApiResult<Void> deleteAssessment(@PathVariable Long id) { svc.del(assessmentMapper, id); return ApiResult.ok(); }

    // ===== 培训 =====
    @ApiOperation("培训列表（分页+筛选）") @GetMapping("/trainings")
    public ApiResult<PageResult<EmployeeTraining>> listTrainings(@Valid PageDTO dto,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<EmployeeTraining> w = new LambdaQueryWrapper<EmployeeTraining>();
        if (StringUtils.isNotBlank(keyword)) w.like(EmployeeTraining::getTrainingTitle, keyword);
        w.orderByDesc(EmployeeTraining::getCreatedAt);
        IPage<EmployeeTraining> r = svc.page(trainingMapper, dto, w);
        return ApiResult.ok(PageResult.of(r)); }
    @OperateLog(module = "人效管理", action = "CREATE", targetType = "TRAINING")
    @ApiOperation("新增培训") @PostMapping("/trainings")
    public ApiResult<Void> createTraining(@Valid @RequestBody EmployeeTraining e) { svc.save(trainingMapper, e); return ApiResult.ok(); }
    @OperateLog(module = "人效管理", action = "SIGN_IN", targetType = "TRAINING", targetIdExpr = "#id")
    @ApiOperation("培训签到") @PostMapping("/trainings/{id}/sign-in")
    public ApiResult<Void> signIn(@PathVariable Long id, @Valid @RequestBody EmployeeTrainingRecord e) { e.setTrainingId(id); svc.save(trainingRecordMapper, e); return ApiResult.ok(); }
    @ApiOperation("培训详情") @GetMapping("/trainings/{id}")
    public ApiResult<EmployeeTraining> getTraining(@PathVariable Long id) { return ApiResult.ok(svc.get(trainingMapper, id, "培训")); }
    @OperateLog(module = "人效管理", action = "UPDATE", targetType = "TRAINING", targetIdExpr = "#id")
    @ApiOperation("更新培训") @PutMapping("/trainings/{id}")
    public ApiResult<Void> updateTraining(@PathVariable Long id, @Valid @RequestBody EmployeeTraining e) { e.setId(id); svc.update(trainingMapper, e); return ApiResult.ok(); }
    @OperateLog(module = "人效管理", action = "DELETE", targetType = "TRAINING", targetIdExpr = "#id")
    @ApiOperation("删除培训") @DeleteMapping("/trainings/{id}")
    public ApiResult<Void> deleteTraining(@PathVariable Long id) { svc.del(trainingMapper, id); return ApiResult.ok(); }

    // ===== 月度复盘 =====
    @ApiOperation("复盘列表（分页+筛选）") @GetMapping("/monthly-reviews")
    public ApiResult<PageResult<EmployeeMonthlyReview>> listReviews(@Valid PageDTO dto,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<EmployeeMonthlyReview> w = new LambdaQueryWrapper<EmployeeMonthlyReview>();
        if (StringUtils.isNotBlank(keyword)) w.like(EmployeeMonthlyReview::getManagerReview, keyword);
        if (StringUtils.isNotBlank(status)) w.eq(EmployeeMonthlyReview::getReviewMonth, status);
        w.orderByDesc(EmployeeMonthlyReview::getCreatedAt);
        IPage<EmployeeMonthlyReview> r = svc.page(monthlyReviewMapper, dto, w);
        return ApiResult.ok(PageResult.of(r)); }
    @OperateLog(module = "人效管理", action = "CREATE", targetType = "MONTHLY_REVIEW")
    @ApiOperation("新增复盘") @PostMapping("/monthly-reviews")
    public ApiResult<Void> createReview(@Valid @RequestBody EmployeeMonthlyReview e) { svc.save(monthlyReviewMapper, e); return ApiResult.ok(); }
    @ApiOperation("复盘详情") @GetMapping("/monthly-reviews/{id}")
    public ApiResult<EmployeeMonthlyReview> getReview(@PathVariable Long id) { return ApiResult.ok(svc.get(monthlyReviewMapper, id, "月度复盘")); }
    @OperateLog(module = "人效管理", action = "UPDATE", targetType = "MONTHLY_REVIEW", targetIdExpr = "#id")
    @ApiOperation("更新复盘") @PutMapping("/monthly-reviews/{id}")
    public ApiResult<Void> updateReview(@PathVariable Long id, @Valid @RequestBody EmployeeMonthlyReview e) { e.setId(id); svc.update(monthlyReviewMapper, e); return ApiResult.ok(); }
    @OperateLog(module = "人效管理", action = "DELETE", targetType = "MONTHLY_REVIEW", targetIdExpr = "#id")
    @ApiOperation("删除复盘") @DeleteMapping("/monthly-reviews/{id}")
    public ApiResult<Void> deleteReview(@PathVariable Long id) { svc.del(monthlyReviewMapper, id); return ApiResult.ok(); }

    // ===== 分层定级 =====
    @ApiOperation("分层列表（分页+筛选）") @GetMapping("/level-records")
    public ApiResult<PageResult<EmployeeLevelRecord>> listLevels(@Valid PageDTO dto,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<EmployeeLevelRecord> w = new LambdaQueryWrapper<EmployeeLevelRecord>();
        if (StringUtils.isNotBlank(keyword)) w.like(EmployeeLevelRecord::getReason, keyword);
        if (StringUtils.isNotBlank(status)) w.eq(EmployeeLevelRecord::getFinalLevel, status);
        w.orderByDesc(EmployeeLevelRecord::getCreatedAt);
        IPage<EmployeeLevelRecord> r = svc.page(levelRecordMapper, dto, w);
        return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("分层详情") @GetMapping("/level-records/{id}")
    public ApiResult<EmployeeLevelRecord> getLevel(@PathVariable Long id) { return ApiResult.ok(svc.get(levelRecordMapper, id, "分层定级")); }
    @OperateLog(module = "人效管理", action = "CREATE", targetType = "LEVEL_RECORD")
    @ApiOperation("新增分层") @PostMapping("/level-records")
    public ApiResult<Void> createLevel(@Valid @RequestBody EmployeeLevelRecord e) { svc.save(levelRecordMapper, e); return ApiResult.ok(); }
    @OperateLog(module = "人效管理", action = "UPDATE", targetType = "LEVEL_RECORD", targetIdExpr = "#id")
    @ApiOperation("更新分层") @PutMapping("/level-records/{id}")
    public ApiResult<Void> updateLevel(@PathVariable Long id, @Valid @RequestBody EmployeeLevelRecord e) { e.setId(id); svc.update(levelRecordMapper, e); return ApiResult.ok(); }
    @OperateLog(module = "人效管理", action = "DELETE", targetType = "LEVEL_RECORD", targetIdExpr = "#id")
    @ApiOperation("删除分层") @DeleteMapping("/level-records/{id}")
    public ApiResult<Void> deleteLevel(@PathVariable Long id) { svc.del(levelRecordMapper, id); return ApiResult.ok(); }

    // ===== 晨夕会 Meetings =====
    @ApiOperation("会议分页列表")
    @GetMapping("/meetings")
    public ApiResult<PageResult<Meeting>> pageMeetings(@Valid PageDTO dto,
            @RequestParam(required = false) String meetingType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Meeting> w = new LambdaQueryWrapper<Meeting>();
        if (StringUtils.isNotBlank(meetingType)) w.eq(Meeting::getType, meetingType);
        if (StringUtils.isNotBlank(status)) w.eq(Meeting::getStatus, status);
        if (StringUtils.isNotBlank(startDate)) w.ge(Meeting::getMeetingDate, startDate);
        if (StringUtils.isNotBlank(endDate)) w.le(Meeting::getMeetingDate, endDate);
        if (StringUtils.isNotBlank(keyword)) w.like(Meeting::getTopic, keyword);
        w.orderByDesc(Meeting::getCreatedAt);
        IPage<Meeting> p = svc.page(meetingMapper, dto, w);
        return ApiResult.ok(PageResult.of(p));
    }

    @OperateLog(module = "人效管理", action = "CREATE", targetType = "MEETING")
    @ApiOperation("新建会议") @PostMapping("/meetings")
    public ApiResult<Void> createMeeting(@Valid @RequestBody Meeting m) { svc.save(meetingMapper, m); return ApiResult.ok(); }

    @OperateLog(module = "人效管理", action = "UPDATE", targetType = "MEETING", targetIdExpr = "#id")
    @ApiOperation("更新会议") @PutMapping("/meetings/{id}")
    public ApiResult<Void> updateMeeting(@PathVariable Long id, @Valid @RequestBody Meeting m) { m.setId(id); svc.update(meetingMapper, m); return ApiResult.ok(); }

    @ApiOperation("会议详情") @GetMapping("/meetings/{id}")
    public ApiResult<Meeting> getMeeting(@PathVariable Long id) { return ApiResult.ok(svc.get(meetingMapper, id, "会议")); }
    @OperateLog(module = "人效管理", action = "DELETE", targetType = "MEETING", targetIdExpr = "#id")
    @ApiOperation("删除会议") @DeleteMapping("/meetings/{id}")
    public ApiResult<Void> deleteMeeting(@PathVariable Long id) { svc.del(meetingMapper, id); return ApiResult.ok(); }
}
