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

    @Transactional public Transaction create(Transaction t) { mapper.insert(t); return t; }

    @Transactional public Transaction update(Long id, Transaction t) { t.setId(id); mapper.updateById(t); return detail(id); }

    @Transactional public void delete(Long id) { mapper.deleteById(id); }

    /** 汇总统计 */
    public Map<String, Object> stats() {
        List<Transaction> all = mapper.selectList(null);
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (Transaction t : all) {
            if (t.getAmount() == null) continue;
            if ("income".equals(t.getType())) totalIncome = totalIncome.add(t.getAmount());
            else if ("expense".equals(t.getType())) totalExpense = totalExpense.add(t.getAmount());
        }
        BigDecimal netProfit = totalIncome.subtract(totalExpense);
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("totalIncome", totalIncome);
        s.put("totalExpense", totalExpense);
        s.put("netProfit", netProfit);
        s.put("receivable", BigDecimal.ZERO);          // TODO: 应收应付模块实现后对接
        s.put("payable", BigDecimal.ZERO);
        s.put("incomeChange", BigDecimal.ZERO);        // TODO: 环比数据待实现
        s.put("expenseChange", BigDecimal.ZERO);
        s.put("profitChange", BigDecimal.ZERO);
        s.put("receivableChange", BigDecimal.ZERO);
        s.put("payableChange", BigDecimal.ZERO);
        return s;
    }
}
