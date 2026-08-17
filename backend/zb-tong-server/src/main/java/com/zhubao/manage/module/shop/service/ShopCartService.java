package com.zhubao.manage.module.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.module.product.entity.Product;
import com.zhubao.manage.module.product.mapper.ProductMapper;
import com.zhubao.manage.module.shop.entity.ShopCart;
import com.zhubao.manage.module.shop.mapper.ShopCartMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShopCartService {

    private final ShopCartMapper cartMapper;
    private final ProductMapper productMapper;

    public ShopCartService(ShopCartMapper cartMapper, ProductMapper productMapper) {
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
    }

    /** 获取用户购物车列表（含商品详情） */
    public List<Map<String, Object>> list(Long userId) {
        List<ShopCart> carts = cartMapper.selectList(
                new LambdaQueryWrapper<ShopCart>().eq(ShopCart::getUserId, userId));
        if (carts.isEmpty()) return Collections.emptyList();

        Set<Long> productIds = carts.stream().map(ShopCart::getProductId).collect(Collectors.toSet());
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Map<String, Object>> result = new ArrayList<>();
        for (ShopCart cart : carts) {
            Product p = productMap.get(cart.getProductId());
            if (p == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", cart.getId());
            item.put("productId", p.getId());
            item.put("productCode", p.getProductCode());
            item.put("productName", p.getProductName());
            item.put("imageUrl", p.getImageUrl());
            item.put("price", p.getRetailPrice());
            item.put("stock", p.getStock());
            item.put("quantity", cart.getQuantity());
            item.put("checked", cart.getChecked() != null ? cart.getChecked() : 1);
            item.put("storeName", p.getStoreName());
            item.put("createdAt", cart.getCreatedAt());
            result.add(item);
        }
        return result;
    }

    /** 添加商品到购物车，已存在则增加数量 */
    @Transactional
    public ShopCart add(Long userId, Long productId, Integer quantity) {
        ShopCart exist = cartMapper.selectOne(new LambdaQueryWrapper<ShopCart>()
                .eq(ShopCart::getUserId, userId)
                .eq(ShopCart::getProductId, productId));
        if (exist != null) {
            exist.setQuantity(exist.getQuantity() + (quantity != null ? quantity : 1));
            exist.setUpdatedAt(LocalDateTime.now());
            cartMapper.updateById(exist);
            return exist;
        }
        ShopCart cart = new ShopCart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(quantity != null ? quantity : 1);
        cart.setChecked(1);
        cartMapper.insert(cart);
        return cart;
    }

    /** 更新数量 */
    public void updateQuantity(Long id, Long userId, Integer quantity) {
        ShopCart cart = cartMapper.selectById(id);
        if (cart != null && cart.getUserId().equals(userId)) {
            cart.setQuantity(Math.max(1, quantity));
            cartMapper.updateById(cart);
        }
    }

    /** 勾选/取消勾选 */
    public void toggleCheck(Long id, Long userId, Integer checked) {
        ShopCart cart = cartMapper.selectById(id);
        if (cart != null && cart.getUserId().equals(userId)) {
            cart.setChecked(checked != null ? checked : 1);
            cartMapper.updateById(cart);
        }
    }

    /** 全选/取消全选 */
    public void checkAll(Long userId, Integer checked) {
        List<ShopCart> carts = cartMapper.selectList(
                new LambdaQueryWrapper<ShopCart>().eq(ShopCart::getUserId, userId));
        for (ShopCart c : carts) {
            c.setChecked(checked != null ? checked : 1);
            cartMapper.updateById(c);
        }
    }

    /** 删除单个 */
    public void remove(Long id, Long userId) {
        cartMapper.delete(new LambdaQueryWrapper<ShopCart>()
                .eq(ShopCart::getId, id)
                .eq(ShopCart::getUserId, userId));
    }

    /** 清空已勾选 */
    public void removeChecked(Long userId) {
        cartMapper.delete(new LambdaQueryWrapper<ShopCart>()
                .eq(ShopCart::getUserId, userId)
                .eq(ShopCart::getChecked, 1));
    }

    /** 同步本地购物车到服务端 */
    @Transactional
    public List<Map<String, Object>> sync(Long userId, List<Map<String, Object>> localItems) {
        if (localItems == null || localItems.isEmpty()) return list(userId);
        for (Map<String, Object> item : localItems) {
            Long productId = toLong(item.get("productId"));
            Integer qty = toInt(item.get("quantity"), 1);
            if (productId != null) {
                add(userId, productId, qty);
            }
        }
        return list(userId);
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.valueOf(v.toString()); } catch (Exception e) { return null; }
    }

    private Integer toInt(Object v, Integer def) {
        if (v == null) return def;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.valueOf(v.toString()); } catch (Exception e) { return def; }
    }
}
