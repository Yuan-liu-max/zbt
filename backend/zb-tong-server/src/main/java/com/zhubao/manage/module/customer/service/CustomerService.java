package com.zhubao.manage.module.customer.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.exception.BusinessException;
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
    public Customer create(Customer c) {
        if (StringUtils.isBlank(c.getCode())) {
            c.setCode("KH" + System.currentTimeMillis() % 100000000);
        }
        checkCodeUnique(c.getCode(), null);
        customerMapper.insert(c); return c;
    }

    @Transactional
    public Customer update(Long id, Customer c) {
        Customer exist = customerMapper.selectById(id);
        if (exist == null) return null;
        if (StringUtils.isNotBlank(c.getCode())) {
            checkCodeUnique(c.getCode(), id);
            exist.setCode(c.getCode());
        }
        if (StringUtils.isNotBlank(c.getName())) exist.setName(c.getName());
        if (StringUtils.isNotBlank(c.getPhone())) exist.setPhone(c.getPhone());
        if (StringUtils.isNotBlank(c.getLevel())) exist.setLevel(c.getLevel());
        if (c.getTotalConsumption() != null) exist.setTotalConsumption(c.getTotalConsumption());
        if (c.getPoints() != null) exist.setPoints(c.getPoints());
        if (StringUtils.isNotBlank(c.getStatus())) exist.setStatus(c.getStatus());
        customerMapper.updateById(exist);
        return exist;
    }

    private void checkCodeUnique(String code, Long excludeId) {
        LambdaQueryWrapper<Customer> w = new LambdaQueryWrapper<Customer>().eq(Customer::getCode, code);
        if (excludeId != null) w.ne(Customer::getId, excludeId);
        if (customerMapper.selectCount(w) > 0) {
            throw new BusinessException(400, "客户编号 [" + code + "] 已存在");
        }
    }

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
        // 从 customer 表真实统计各等级会员数
        List<Customer> allCustomers = customerMapper.selectList(null);
        Map<String, Long> countByIdentifier = allCustomers.stream()
                .filter(c -> StringUtils.isNotBlank(c.getLevel()))
                .collect(java.util.stream.Collectors.groupingBy(Customer::getLevel, java.util.stream.Collectors.counting()));
        Map<String, Object> stats = new LinkedHashMap<>();
        long total = countByIdentifier.values().stream().mapToLong(Long::longValue).sum();
        stats.put("totalCount", total);
        stats.put("vipCount", countByIdentifier.getOrDefault("vip", 0L));
        stats.put("normalCount", countByIdentifier.getOrDefault("normal", 0L));
        stats.put("diamondCount", countByIdentifier.getOrDefault("diamond", 0L));
        return stats;
    }

    @Transactional
    public MemberLevel createLevel(MemberLevel ml) { memberLevelMapper.insert(ml); return ml; }

    @Transactional
    public MemberLevel updateLevel(Long id, MemberLevel ml) { ml.setId(id); memberLevelMapper.updateById(ml); return memberLevelMapper.selectById(id); }
}
