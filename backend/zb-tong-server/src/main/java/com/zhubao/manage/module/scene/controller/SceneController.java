package com.zhubao.manage.module.scene.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.scene.entity.*;
import com.zhubao.manage.module.scene.mapper.*;
import com.zhubao.manage.module.scene.service.SceneService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "场景运营")
@RestController
@RequestMapping("/scenes")
@org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
public class SceneController {

    private final SceneService svc;
    private final SceneHealthInspectionMapper healthMapper;
    private final SceneDisplayInspectionMapper displayMapper;
    private final SceneMaterialUpdateMapper materialMapper;
    private final SceneEquipmentCheckMapper equipmentMapper;
    private final SceneCustomerExperienceReviewMapper experienceMapper;

    public SceneController(SceneService svc, SceneHealthInspectionMapper hm,
                           SceneDisplayInspectionMapper dm, SceneMaterialUpdateMapper mm,
                           SceneEquipmentCheckMapper em, SceneCustomerExperienceReviewMapper xm) {
        this.svc = svc; this.healthMapper = hm; this.displayMapper = dm;
        this.materialMapper = mm; this.equipmentMapper = em; this.experienceMapper = xm;
    }

    // ===== 卫生巡检 =====
    @ApiOperation("卫生巡检分页") @GetMapping("/health-inspections")
    public ApiResult<PageResult<SceneHealthInspection>> pageHealth(@Valid PageDTO dto) {
        IPage<SceneHealthInspection> r = svc.page(healthMapper, dto, new LambdaQueryWrapper<>()); return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("卫生巡检详情") @GetMapping("/health-inspections/{id}")
    public ApiResult<SceneHealthInspection> getHealth(@PathVariable Long id) { return ApiResult.ok(svc.get(healthMapper, id, "卫生巡检")); }
    @ApiOperation("新增卫生巡检") @PostMapping("/health-inspections")
    public ApiResult<Void> createHealth(@Valid @RequestBody SceneHealthInspection e) { svc.save(healthMapper, e); return ApiResult.ok(); }
    @ApiOperation("更新卫生巡检") @PutMapping("/health-inspections/{id}")
    public ApiResult<Void> updateHealth(@PathVariable Long id, @Valid @RequestBody SceneHealthInspection e) { e.setId(id); svc.update(healthMapper, e); return ApiResult.ok(); }
    @ApiOperation("删除卫生巡检") @DeleteMapping("/health-inspections/{id}")
    public ApiResult<Void> deleteHealth(@PathVariable Long id) { svc.del(healthMapper, id); return ApiResult.ok(); }

    // ===== 陈列检查 =====
    @ApiOperation("陈列检查分页") @GetMapping("/display-inspections")
    public ApiResult<PageResult<SceneDisplayInspection>> pageDisplay(@Valid PageDTO dto) {
        IPage<SceneDisplayInspection> r = svc.page(displayMapper, dto, new LambdaQueryWrapper<>()); return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("陈列检查详情") @GetMapping("/display-inspections/{id}")
    public ApiResult<SceneDisplayInspection> getDisplay(@PathVariable Long id) { return ApiResult.ok(svc.get(displayMapper, id, "陈列检查")); }
    @ApiOperation("新增陈列检查") @PostMapping("/display-inspections")
    public ApiResult<Void> createDisplay(@Valid @RequestBody SceneDisplayInspection e) { svc.save(displayMapper, e); return ApiResult.ok(); }
    @ApiOperation("更新陈列检查") @PutMapping("/display-inspections/{id}")
    public ApiResult<Void> updateDisplay(@PathVariable Long id, @Valid @RequestBody SceneDisplayInspection e) { e.setId(id); svc.update(displayMapper, e); return ApiResult.ok(); }
    @ApiOperation("删除陈列检查") @DeleteMapping("/display-inspections/{id}")
    public ApiResult<Void> deleteDisplay(@PathVariable Long id) { svc.del(displayMapper, id); return ApiResult.ok(); }

    // ===== 物料更新 =====
    @ApiOperation("物料更新分页") @GetMapping("/material-updates")
    public ApiResult<PageResult<SceneMaterialUpdate>> pageMaterial(@Valid PageDTO dto) {
        IPage<SceneMaterialUpdate> r = svc.page(materialMapper, dto, new LambdaQueryWrapper<>()); return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("物料更新详情") @GetMapping("/material-updates/{id}")
    public ApiResult<SceneMaterialUpdate> getMaterial(@PathVariable Long id) { return ApiResult.ok(svc.get(materialMapper, id, "物料更新")); }
    @ApiOperation("新增物料更新") @PostMapping("/material-updates")
    public ApiResult<Void> createMaterial(@Valid @RequestBody SceneMaterialUpdate e) { svc.save(materialMapper, e); return ApiResult.ok(); }
    @ApiOperation("更新物料更新") @PutMapping("/material-updates/{id}")
    public ApiResult<Void> updateMaterial(@PathVariable Long id, @Valid @RequestBody SceneMaterialUpdate e) { e.setId(id); svc.update(materialMapper, e); return ApiResult.ok(); }
    @ApiOperation("删除物料更新") @DeleteMapping("/material-updates/{id}")
    public ApiResult<Void> deleteMaterial(@PathVariable Long id) { svc.del(materialMapper, id); return ApiResult.ok(); }

    // ===== 设备检查 =====
    @ApiOperation("设备检查分页") @GetMapping("/equipment-checks")
    public ApiResult<PageResult<SceneEquipmentCheck>> pageEquipment(@Valid PageDTO dto) {
        IPage<SceneEquipmentCheck> r = svc.page(equipmentMapper, dto, new LambdaQueryWrapper<>()); return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("设备检查详情") @GetMapping("/equipment-checks/{id}")
    public ApiResult<SceneEquipmentCheck> getEquipment(@PathVariable Long id) { return ApiResult.ok(svc.get(equipmentMapper, id, "设备检查")); }
    @ApiOperation("新增设备检查") @PostMapping("/equipment-checks")
    public ApiResult<Void> createEquipment(@Valid @RequestBody SceneEquipmentCheck e) { svc.save(equipmentMapper, e); return ApiResult.ok(); }
    @ApiOperation("更新设备检查") @PutMapping("/equipment-checks/{id}")
    public ApiResult<Void> updateEquipment(@PathVariable Long id, @Valid @RequestBody SceneEquipmentCheck e) { e.setId(id); svc.update(equipmentMapper, e); return ApiResult.ok(); }
    @ApiOperation("删除设备检查") @DeleteMapping("/equipment-checks/{id}")
    public ApiResult<Void> deleteEquipment(@PathVariable Long id) { svc.del(equipmentMapper, id); return ApiResult.ok(); }

    // ===== 客户体验复盘 =====
    @ApiOperation("客户体验复盘分页") @GetMapping("/experience-reviews")
    public ApiResult<PageResult<SceneCustomerExperienceReview>> pageExperience(@Valid PageDTO dto) {
        IPage<SceneCustomerExperienceReview> r = svc.page(experienceMapper, dto, new LambdaQueryWrapper<>()); return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("客户体验复盘详情") @GetMapping("/experience-reviews/{id}")
    public ApiResult<SceneCustomerExperienceReview> getExperience(@PathVariable Long id) { return ApiResult.ok(svc.get(experienceMapper, id, "客户体验复盘")); }
    @ApiOperation("新增客户体验复盘") @PostMapping("/experience-reviews")
    public ApiResult<Void> createExperience(@Valid @RequestBody SceneCustomerExperienceReview e) { svc.save(experienceMapper, e); return ApiResult.ok(); }
    @ApiOperation("更新客户体验复盘") @PutMapping("/experience-reviews/{id}")
    public ApiResult<Void> updateExperience(@PathVariable Long id, @Valid @RequestBody SceneCustomerExperienceReview e) { e.setId(id); svc.update(experienceMapper, e); return ApiResult.ok(); }
    @ApiOperation("删除客户体验复盘") @DeleteMapping("/experience-reviews/{id}")
    public ApiResult<Void> deleteExperience(@PathVariable Long id) { svc.del(experienceMapper, id); return ApiResult.ok(); }
}
