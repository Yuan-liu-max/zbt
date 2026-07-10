package com.zhubao.manage.module.sales.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.sales.dto.SalesCreateDTO;
import com.zhubao.manage.module.sales.entity.SalesItem;
import com.zhubao.manage.module.sales.entity.SalesRecord;
import com.zhubao.manage.module.sales.mapper.SalesItemMapper;
import com.zhubao.manage.module.sales.mapper.SalesRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalesService {

    private final SalesRecordMapper recordMapper;
    private final SalesItemMapper itemMapper;
    private final UserContextHolder userContextHolder;

    public SalesService(SalesRecordMapper rm, SalesItemMapper im, UserContextHolder uc) {
        this.recordMapper = rm; this.itemMapper = im; this.userContextHolder = uc;
    }

    /** 销售录入 —— 同事务 */
    @Transactional
    public SalesRecord create(SalesCreateDTO dto) {
        SalesRecord rec = new SalesRecord();
        rec.setSalesNo("SL" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%06d", System.currentTimeMillis() % 1000000));
        rec.setStoreId(dto.getStoreId());
        rec.setEmployeeId(dto.getEmployeeId());
        rec.setSalesDate(LocalDate.parse(dto.getSalesDate()));
        rec.setOrderNo(dto.getOrderNo());
        rec.setTotalAmount(dto.getTotalAmount());
        rec.setPaidAmount(dto.getPaidAmount());
        rec.setProductCount(dto.getItems().size());
        rec.setCustomerType(dto.getCustomerType());
        rec.setCustomerGender(dto.getCustomerGender());
        rec.setCustomerAgeRange(dto.getCustomerAgeRange());
        rec.setPurchaseScene(dto.getPurchaseScene());
        rec.setCustomerConcern(dto.getCustomerConcern());
        rec.setSalesPhotoUrls(dto.getSalesPhotoUrls());
        rec.setRemark(dto.getRemark());
        rec.setAuditStatus("PENDING");
        recordMapper.insert(rec);

        for (SalesCreateDTO.SalesItemDTO si : dto.getItems()) {
            SalesItem item = new SalesItem();
            item.setSalesRecordId(rec.getId());
            item.setProductId(si.getProductId());
            item.setProductName(si.getProductName());
            item.setCategory(si.getCategory());
            item.setStyle(si.getStyle());
            item.setMaterial(si.getMaterial());
            item.setWeight(si.getWeight());
            item.setSize(si.getSize());
            item.setPrice(si.getPrice());
            item.setQuantity(si.getQuantity() != null ? si.getQuantity() : 1);
            item.setGrossMarginRate(si.getGrossMarginRate());
            item.setCustomerFavoritePoint(si.getCustomerFavoritePoint());
            item.setObjection(si.getObjection());
            item.setClosingReason(si.getClosingReason());
            itemMapper.insert(item);
        }
        return rec;
    }

    /** 审核 */
    @Transactional
    public void audit(Long id, String auditStatus, String comment) {
        SalesRecord rec = recordMapper.selectById(id);
        if (rec == null) throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "销售记录不存在");
        if (!"PENDING".equals(rec.getAuditStatus())) throw new BusinessException(400, "该记录已审核");
        rec.setAuditStatus(auditStatus);
        rec.setAuditComment(comment);
        rec.setAuditorId(userContextHolder.getUserId());
        rec.setAuditedAt(LocalDateTime.now());
        recordMapper.updateById(rec);
    }

    // ---- 统计 ----

    public IPage<SalesRecord> page(PageDTO dto) {
        return recordMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()),
                new LambdaQueryWrapper<SalesRecord>().orderByDesc(SalesRecord::getSalesDate));
    }

    public SalesRecord detail(Long id) {
        SalesRecord rec = recordMapper.selectById(id);
        if (rec == null) throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "销售记录不存在");
        return rec;
    }

    public List<SalesItem> getItems(Long recordId) {
        return itemMapper.selectList(new LambdaQueryWrapper<SalesItem>().eq(SalesItem::getSalesRecordId, recordId));
    }

    /** 员工指标 */
    public Map<String, Object> employeeMetrics(Long employeeId, String month) {
        List<SalesRecord> records = queryByMonth(month, null, employeeId);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalAmount", records.stream().map(SalesRecord::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        m.put("orderCount", records.size());
        m.put("avgOrderAmount", records.isEmpty() ? BigDecimal.ZERO
                : records.stream().map(SalesRecord::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(records.size()), 2, BigDecimal.ROUND_HALF_UP));
        return m;
    }

    /** 门店指标 */
    public Map<String, Object> storeMetrics(Long storeId, String month) {
        List<SalesRecord> records = queryByMonth(month, storeId, null);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalAmount", records.stream().map(SalesRecord::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        m.put("orderCount", records.size());
        m.put("employeeCount", records.stream().map(SalesRecord::getEmployeeId).distinct().count());
        return m;
    }

    /** 员工排行 */
    public List<Map<String, Object>> employeeRanking(String month, int topN) {
        return queryByMonth(month, null, null).stream()
                .collect(Collectors.groupingBy(SalesRecord::getEmployeeId,
                        Collectors.reducing(BigDecimal.ZERO, SalesRecord::getTotalAmount, BigDecimal::add)))
                .entrySet().stream().sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(topN).map(e -> { Map<String,Object> m=new LinkedHashMap<>(); m.put("employeeId",e.getKey()); m.put("amount",e.getValue()); return m; })
                .collect(Collectors.toList());
    }

    /** 门店排行 */
    public List<Map<String, Object>> storeRanking(String month) {
        return queryByMonth(month, null, null).stream()
                .collect(Collectors.groupingBy(SalesRecord::getStoreId,
                        Collectors.reducing(BigDecimal.ZERO, SalesRecord::getTotalAmount, BigDecimal::add)))
                .entrySet().stream().sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(e -> { Map<String,Object> m=new LinkedHashMap<>(); m.put("storeId",e.getKey()); m.put("amount",e.getValue()); return m; })
                .collect(Collectors.toList());
    }

    /** 品类结构 */
    public List<Map<String, Object>> categoryStructure(String month, Long storeId) {
        List<SalesRecord> records = queryByMonth(month, storeId, null);
        List<Long> recordIds = records.stream().map(SalesRecord::getId).collect(Collectors.toList());
        if (recordIds.isEmpty()) return Collections.emptyList();
        return itemMapper.selectList(new LambdaQueryWrapper<SalesItem>().in(SalesItem::getSalesRecordId, recordIds))
                .stream().collect(Collectors.groupingBy(SalesItem::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, si -> si.getPrice().multiply(BigDecimal.valueOf(si.getQuantity())), BigDecimal::add)))
                .entrySet().stream().map(e -> { Map<String,Object> m=new LinkedHashMap<>(); m.put("category",e.getKey()); m.put("amount",e.getValue()); return m; })
                .collect(Collectors.toList());
    }

    private List<SalesRecord> queryByMonth(String month, Long storeId, Long employeeId) {
        LambdaQueryWrapper<SalesRecord> w = new LambdaQueryWrapper<>();
        if (month != null) {
            w.ge(SalesRecord::getSalesDate, month + "-01");
            // P2-18 fix: 正确计算月末边界
            java.time.LocalDate monthEnd = java.time.LocalDate.parse(month + "-01").plusMonths(1).minusDays(1);
            w.le(SalesRecord::getSalesDate, monthEnd.toString());
        }
        if (storeId != null) w.eq(SalesRecord::getStoreId, storeId);
        if (employeeId != null) w.eq(SalesRecord::getEmployeeId, employeeId);
        return recordMapper.selectList(w);
    }
}
