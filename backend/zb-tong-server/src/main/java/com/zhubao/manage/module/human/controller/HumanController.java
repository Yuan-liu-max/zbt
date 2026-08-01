package com.zhubao.manage.module.human.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.human.entity.*;
import com.zhubao.manage.module.human.mapper.*;
import com.zhubao.manage.module.human.service.HumanService;
import javax.validation.Valid;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    public HumanController(HumanService svc, EmployeeProfileMapper p, EmployeeInterviewMapper i,
                           EmployeeAssessmentMapper a, EmployeeTrainingMapper t,
                           EmployeeTrainingRecordMapper tr, EmployeeMonthlyReviewMapper mr,
                           EmployeeLevelRecordMapper lr, MeetingMapper mm) {
        this.svc = svc; this.profileMapper = p; this.interviewMapper = i;
        this.assessmentMapper = a; this.trainingMapper = t; this.trainingRecordMapper = tr;
        this.monthlyReviewMapper = mr; this.levelRecordMapper = lr; this.meetingMapper = mm;
    }

    // ===== 聚合档案 =====
    @ApiOperation("员工完整档案")
    @GetMapping("/employees/{userId}/profile")
    public ApiResult<Map<String, Object>> profile(@PathVariable Long userId) {
        return ApiResult.ok(svc.getEmployeeProfile(userId));
    }

    // ===== 晨会/夕会 (EmployeeProfile 管理) =====
    @ApiOperation("员工档案列表") @GetMapping("/employees")
    public ApiResult<List<EmployeeProfile>> listEmployees() { return ApiResult.ok(svc.all(profileMapper, new LambdaQueryWrapper<>())); }
    @ApiOperation("新增员工档案") @PostMapping("/employees")
    public ApiResult<Void> createEmployee(@Valid @RequestBody EmployeeProfile e) { svc.save(profileMapper, e); return ApiResult.ok(); }
    @ApiOperation("更新员工档案") @PutMapping("/employees/{id}")
    public ApiResult<Void> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeProfile e) { e.setId(id); svc.update(profileMapper, e); return ApiResult.ok(); }

    // ===== 面谈 =====
    @ApiOperation("面谈列表") @GetMapping("/interviews")
    public ApiResult<List<EmployeeInterview>> listInterviews() { return ApiResult.ok(svc.all(interviewMapper, new LambdaQueryWrapper<>())); }
    @ApiOperation("分页面谈") @GetMapping("/interviews/page")
    public ApiResult<PageResult<EmployeeInterview>> pageInterviews(PageDTO dto) {
        IPage<EmployeeInterview> r = svc.page(interviewMapper, dto, new LambdaQueryWrapper<>()); return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("新增面谈") @PostMapping("/interviews")
    public ApiResult<Void> createInterview(@Valid @RequestBody EmployeeInterview e) { svc.save(interviewMapper, e); return ApiResult.ok(); }
    @ApiOperation("更新面谈") @PutMapping("/interviews/{id}")
    public ApiResult<Void> updateInterview(@PathVariable Long id, @Valid @RequestBody EmployeeInterview e) { e.setId(id); svc.update(interviewMapper, e); return ApiResult.ok(); }
    @ApiOperation("删除面谈") @DeleteMapping("/interviews/{id}")
    public ApiResult<Void> deleteInterview(@PathVariable Long id) { svc.del(interviewMapper, id); return ApiResult.ok(); }

    // ===== 考核 =====
    @ApiOperation("考核列表") @GetMapping("/assessments")
    public ApiResult<List<EmployeeAssessment>> listAssessments() { return ApiResult.ok(svc.all(assessmentMapper, new LambdaQueryWrapper<>())); }
    @ApiOperation("新增考核") @PostMapping("/assessments")
    public ApiResult<Void> createAssessment(@Valid @RequestBody EmployeeAssessment e) { svc.save(assessmentMapper, e); return ApiResult.ok(); }
    @ApiOperation("更新考核") @PutMapping("/assessments/{id}")
    public ApiResult<Void> updateAssessment(@PathVariable Long id, @Valid @RequestBody EmployeeAssessment e) { e.setId(id); svc.update(assessmentMapper, e); return ApiResult.ok(); }

    // ===== 培训 =====
    @ApiOperation("培训列表") @GetMapping("/trainings")
    public ApiResult<List<EmployeeTraining>> listTrainings() { return ApiResult.ok(svc.all(trainingMapper, new LambdaQueryWrapper<>())); }
    @ApiOperation("新增培训") @PostMapping("/trainings")
    public ApiResult<Void> createTraining(@Valid @RequestBody EmployeeTraining e) { svc.save(trainingMapper, e); return ApiResult.ok(); }
    @ApiOperation("培训签到") @PostMapping("/trainings/{id}/sign-in")
    public ApiResult<Void> signIn(@PathVariable Long id, @Valid @RequestBody EmployeeTrainingRecord e) { e.setTrainingId(id); svc.save(trainingRecordMapper, e); return ApiResult.ok(); }

    // ===== 月度复盘 =====
    @ApiOperation("复盘列表") @GetMapping("/monthly-reviews")
    public ApiResult<List<EmployeeMonthlyReview>> listReviews() { return ApiResult.ok(svc.all(monthlyReviewMapper, new LambdaQueryWrapper<>())); }
    @ApiOperation("新增复盘") @PostMapping("/monthly-reviews")
    public ApiResult<Void> createReview(@Valid @RequestBody EmployeeMonthlyReview e) { svc.save(monthlyReviewMapper, e); return ApiResult.ok(); }

    // ===== 分层定级 =====
    @ApiOperation("分层列表") @GetMapping("/level-records")
    public ApiResult<List<EmployeeLevelRecord>> listLevels() { return ApiResult.ok(svc.all(levelRecordMapper, new LambdaQueryWrapper<>())); }
    @ApiOperation("新增分层") @PostMapping("/level-records")
    public ApiResult<Void> createLevel(@Valid @RequestBody EmployeeLevelRecord e) { svc.save(levelRecordMapper, e); return ApiResult.ok(); }

    // ===== 晨夕会 Meetings =====
    @ApiOperation("会议分页列表")
    @GetMapping("/meetings")
    public ApiResult<PageResult<Meeting>> pageMeetings(@Valid PageDTO dto,
            @RequestParam(required = false) String meetingType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Meeting> w = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(meetingType)) w.eq(Meeting::getType, meetingType);
        if (StringUtils.isNotBlank(status)) w.eq(Meeting::getStatus, status);
        if (StringUtils.isNotBlank(startDate)) w.ge(Meeting::getMeetingDate, startDate);
        if (StringUtils.isNotBlank(endDate)) w.le(Meeting::getMeetingDate, endDate);
        if (StringUtils.isNotBlank(keyword)) w.like(Meeting::getTopic, keyword);
        w.orderByDesc(Meeting::getCreatedAt);
        IPage<Meeting> p = svc.page(meetingMapper, dto, w);
        return ApiResult.ok(PageResult.of(p));
    }

    @ApiOperation("新建会议") @PostMapping("/meetings")
    public ApiResult<Void> createMeeting(@Valid @RequestBody Meeting m) { svc.save(meetingMapper, m); return ApiResult.ok(); }

    @ApiOperation("更新会议") @PutMapping("/meetings/{id}")
    public ApiResult<Void> updateMeeting(@PathVariable Long id, @Valid @RequestBody Meeting m) { m.setId(id); svc.update(meetingMapper, m); return ApiResult.ok(); }

    @ApiOperation("会议详情") @GetMapping("/meetings/{id}")
    public ApiResult<Meeting> getMeeting(@PathVariable Long id) { return ApiResult.ok(svc.get(meetingMapper, id, "会议")); }
}
