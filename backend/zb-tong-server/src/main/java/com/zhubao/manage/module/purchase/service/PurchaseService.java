package com.zhubao.manage.module.purchase.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.purchase.entity.PurchaseItem;
import com.zhubao.manage.module.purchase.entity.PurchaseOrder;
import com.zhubao.manage.module.purchase.mapper.PurchaseItemMapper;
import com.zhubao.manage.module.purchase.mapper.PurchaseOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseOrderMapper orderMapper;
    private final PurchaseItemMapper itemMapper;
    private final UserContextHolder userContextHolder;

    public PurchaseService(PurchaseOrderMapper om, PurchaseItemMapper im, UserContextHolder uch) {
        this.orderMapper = om; this.itemMapper = im; this.userContextHolder = uch;
    }

    public IPage<PurchaseOrder> page(PageDTO dto) {
        return orderMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()),
                new LambdaQueryWrapper<PurchaseOrder>().orderByDesc(PurchaseOrder::getCreatedAt));
    }

    public PurchaseOrder detail(Long id) {
        PurchaseOrder o = orderMapper.selectById(id);
        if (o == null) throw new BusinessException(404, "采购单不存在");
        return o;
    }

    public List<PurchaseItem> items(Long orderId) {
        return itemMapper.selectList(new LambdaQueryWrapper<PurchaseItem>().eq(PurchaseItem::getOrderId, orderId));
    }

    @Transactional
    public PurchaseOrder create(PurchaseOrder order, List<PurchaseItem> items) {
        order.setStatus("DRAFT");
        orderMapper.insert(order);
        if (items != null) {
            for (PurchaseItem item : items) {
                item.setOrderId(order.getId());
                itemMapper.insert(item);
            }
        }
        return order;
    }

    @Transactional
    public void update(Long id, PurchaseOrder order) {
        detail(id); order.setId(id); orderMapper.updateById(order);
    }

    @Transactional
    public void submit(Long id) {
        PurchaseOrder o = detail(id);
        if (!"DRAFT".equals(o.getStatus())) throw new BusinessException(400, "仅草稿可提交");
        o.setStatus("SUBMITTED");
        orderMapper.updateById(o);
    }

    @Transactional
    public void approve(Long id) {
        PurchaseOrder o = detail(id);
        if (!"SUBMITTED".equals(o.getStatus())) throw new BusinessException(400, "仅已提交可审核");
        o.setStatus("APPROVED");
        o.setApproverId(userContextHolder.getUserId());
        orderMapper.updateById(o);
    }

    @Transactional
    public void reject(Long id) {
        PurchaseOrder o = detail(id);
        if (!"SUBMITTED".equals(o.getStatus())) throw new BusinessException(400, "仅已提交可审核");
        o.setStatus("REJECTED");
        o.setApproverId(userContextHolder.getUserId());
        orderMapper.updateById(o);
    }

    @Transactional
    public void delete(Long id) { orderMapper.deleteById(id); }
}
