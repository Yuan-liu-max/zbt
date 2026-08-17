package com.zhubao.manage.module.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.module.finance.entity.Transaction;
import com.zhubao.manage.module.finance.mapper.TransactionMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class FinanceService {

    private final TransactionMapper mapper;
    public FinanceService(TransactionMapper m) { this.mapper = m; }

    public IPage<Transaction> page(PageDTO dto, String type, String startDate, String endDate,
                                    String account, String relatedObject, String keyword) {
        LambdaQueryWrapper<Transaction> w = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(type)) w.eq(Transaction::getType, type);
        if (StringUtils.isNotBlank(startDate)) w.ge(Transaction::getTransactionDate, startDate);
        if (StringUtils.isNotBlank(endDate)) w.le(Transaction::getTransactionDate, endDate);
        if (StringUtils.isNotBlank(account)) w.like(Transaction::getAccount, account);
        if (StringUtils.isNotBlank(relatedObject)) w.like(Transaction::getRelatedObject, relatedObject);
        if (StringUtils.isNotBlank(keyword)) w.and(x -> x.like(Transaction::getCode, keyword).or().like(Transaction::getRemark, keyword));
        w.orderByDesc(Transaction::getTransactionDate);
        return mapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), w);
    }

    public Transaction detail(Long id) { return mapper.selectById(id); }

    @Transactional public Transaction create(Transaction t) {
        t.setCode("TXN" + System.currentTimeMillis());
        mapper.insert(t);
        return t;
    }

    @Transactional public Transaction update(Long id, Transaction t) { t.setId(id); mapper.updateById(t); return detail(id); }

    @Transactional public void delete(Long id) { mapper.deleteById(id); }

    /** 汇总统计 — SELECT SUM(amount) WHERE type='income' / 'expense' */
    public Map<String, Object> stats() {
        Map<String, Object> incomeRow = mapper.selectMaps(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Transaction>()
                        .select("IFNULL(SUM(amount),0) AS income").eq("type", "income")).stream()
                .findFirst().orElse(new java.util.HashMap<>());
        Map<String, Object> expenseRow = mapper.selectMaps(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Transaction>()
                        .select("IFNULL(SUM(amount),0) AS expense").eq("type", "expense")).stream()
                .findFirst().orElse(new java.util.HashMap<>());
        BigDecimal totalIncome = incomeRow.get("income") instanceof BigDecimal
                ? (BigDecimal) incomeRow.get("income") : BigDecimal.ZERO;
        BigDecimal totalExpense = expenseRow.get("expense") instanceof BigDecimal
                ? (BigDecimal) expenseRow.get("expense") : BigDecimal.ZERO;
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("totalIncome", totalIncome);
        s.put("totalExpense", totalExpense);
        s.put("netProfit", totalIncome.subtract(totalExpense));
        return s;
    }
}
