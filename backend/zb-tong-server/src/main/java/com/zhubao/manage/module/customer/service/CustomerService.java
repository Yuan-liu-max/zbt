package com.zhubao.manage.module.customer.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.module.customer.entity.Customer;
import com.zhubao.manage.module.customer.entity.MemberLevel;
import com.zhubao.manage.module.customer.mapper.CustomerMapper;
import com.zhubao.manage.module.customer.mapper.MemberLevelMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CustomerService {

    private final CustomerMapper customerMapper;
    private final MemberLevelMapper memberLevelMapper;

    public CustomerService(CustomerMapper cm, MemberLevelMapper mlm) { this.customerMapper = cm; this.memberLevelMapper = mlm; }

    // ==================== 客户 ====================

    public IPage<Customer> pageCustomers(PageDTO dto, String name, String phone, String level, String startDate, String endDate) {
        LambdaQueryWrapper<Customer> w = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(name)) w.like(Customer::getName, name);
        if (StringUtils.isNotBlank(phone)) w.like(Customer::getPhone, phone);
        if (StringUtils.isNotBlank(level)) w.eq(Customer::getLevel, level);
        if (StringUtils.isNotBlank(startDate)) w.ge(Customer::getRegisteredAt, startDate);
        if (StringUtils.isNotBlank(endDate)) w.le(Customer::getRegisteredAt, endDate);
        w.orderByDesc(Customer::getCreatedAt);
        return customerMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), w);
    }

    public Customer detail(Long id) { return customerMapper.selectById(id); }

    @Transactional
    public Customer create(Customer c) { customerMapper.insert(c); return c; }

    @Transactional
    public Customer update(Long id, Customer c) { c.setId(id); customerMapper.updateById(c); return detail(id); }

    @Transactional
    public void delete(Long id) { customerMapper.deleteById(id); }

    // ==================== 会员等级 ====================

    public IPage<MemberLevel> pageLevels(PageDTO dto, String level, String name, String status) {
        LambdaQueryWrapper<MemberLevel> w = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(level)) w.eq(MemberLevel::getIdentifier, level);
        if (StringUtils.isNotBlank(name)) w.like(MemberLevel::getName, name);
        if (StringUtils.isNotBlank(status)) w.eq(MemberLevel::getStatus, status);
        return memberLevelMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), w);
    }

    public Map<String, Object> levelStats() {
        List<MemberLevel> all = memberLevelMapper.selectList(null);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCount", all.stream().mapToInt(MemberLevel::getMemberCount).sum());
        for (MemberLevel ml : all) {
            stats.put(ml.getIdentifier() + "Count", ml.getMemberCount());
        }
        return stats;
    }

    @Transactional
    public MemberLevel createLevel(MemberLevel ml) { memberLevelMapper.insert(ml); return ml; }

    @Transactional
    public MemberLevel updateLevel(Long id, MemberLevel ml) { ml.setId(id); memberLevelMapper.updateById(ml); return memberLevelMapper.selectById(id); }
}
