package com.zhubao.manage.module.certificate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.module.certificate.entity.Certificate;
import com.zhubao.manage.module.certificate.mapper.CertificateMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CertificateService {

    private final CertificateMapper mapper;
    public CertificateService(CertificateMapper m) { this.mapper = m; }

    public IPage<Certificate> page(PageDTO dto, String code, String type, String status, String issuer, String startDate, String endDate) {
        LambdaQueryWrapper<Certificate> w = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(code)) w.like(Certificate::getCode, code);
        if (StringUtils.isNotBlank(type)) w.eq(Certificate::getType, type);
        if (StringUtils.isNotBlank(status)) w.eq(Certificate::getStatus, status);
        if (StringUtils.isNotBlank(issuer)) w.like(Certificate::getIssuer, issuer);
        if (StringUtils.isNotBlank(startDate)) w.ge(Certificate::getIssueDate, startDate);
        if (StringUtils.isNotBlank(endDate)) w.le(Certificate::getExpiryDate, endDate);
        w.orderByDesc(Certificate::getCreatedAt);
        return mapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), w);
    }

    public Certificate detail(Long id) { return mapper.selectById(id); }
    @Transactional public Certificate create(Certificate c) { mapper.insert(c); return c; }
    @Transactional public Certificate update(Long id, Certificate c) { c.setId(id); mapper.updateById(c); return detail(id); }
    @Transactional public void delete(Long id) { mapper.deleteById(id); }
}
