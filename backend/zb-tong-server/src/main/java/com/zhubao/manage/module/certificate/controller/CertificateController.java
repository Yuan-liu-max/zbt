package com.zhubao.manage.module.certificate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.certificate.entity.Certificate;
import com.zhubao.manage.module.certificate.service.CertificateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "证书管理")
@RestController
@RequestMapping("/certificates")
@PreAuthorize("isAuthenticated()")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService cs) { this.certificateService = cs; }

    @ApiOperation("证书分页列表")
    @GetMapping
    public ApiResult<PageResult<Certificate>> list(@Valid PageDTO dto,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String issuer,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<Certificate> p = certificateService.page(dto, code, type, status, issuer, startDate, endDate);
        return ApiResult.ok(PageResult.of(p));
    }

    @ApiOperation("证书详情") @GetMapping("/{id}")
    public ApiResult<Certificate> detail(@PathVariable Long id) { return ApiResult.ok(certificateService.detail(id)); }

    @ApiOperation("新增证书") @PostMapping
    public ApiResult<Certificate> create(@Valid @RequestBody Certificate c) { return ApiResult.ok(certificateService.create(c)); }

    @ApiOperation("更新证书") @PutMapping("/{id}")
    public ApiResult<Certificate> update(@PathVariable Long id, @Valid @RequestBody Certificate c) { return ApiResult.ok(certificateService.update(id, c)); }

    @ApiOperation("删除证书") @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) { certificateService.delete(id); return ApiResult.ok(); }
}
