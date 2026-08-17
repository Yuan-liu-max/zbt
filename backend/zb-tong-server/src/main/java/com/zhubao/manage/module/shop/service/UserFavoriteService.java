package com.zhubao.manage.module.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.module.product.entity.Product;
import com.zhubao.manage.module.product.mapper.ProductMapper;
import com.zhubao.manage.module.shop.entity.UserFavorite;
import com.zhubao.manage.module.shop.mapper.UserFavoriteMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserFavoriteService {

    private final UserFavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;

    public UserFavoriteService(UserFavoriteMapper favoriteMapper, ProductMapper productMapper) {
        this.favoriteMapper = favoriteMapper;
        this.productMapper = productMapper;
    }

    /** 获取用户收藏商品列表 */
    public List<Map<String, Object>> list(Long userId) {
        List<UserFavorite> favs = favoriteMapper.selectList(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .orderByDesc(UserFavorite::getCreatedAt));
        if (favs.isEmpty()) return Collections.emptyList();

        Set<Long> productIds = favs.stream().map(UserFavorite::getProductId).collect(Collectors.toSet());
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<Map<String, Object>> result = new ArrayList<>();
        for (UserFavorite fav : favs) {
            Product p = productMap.get(fav.getProductId());
            if (p == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", fav.getId());
            item.put("productId", p.getId());
            item.put("productCode", p.getProductCode());
            item.put("productName", p.getProductName());
            item.put("price", p.getRetailPrice());
            item.put("imageUrl", null);
            item.put("status", "ON_SALE".equals(p.getStatus()) ? "on" : "off");
            item.put("storeName", p.getStoreName());
            item.put("createdAt", fav.getCreatedAt());
            result.add(item);
        }
        return result;
    }

    /** 添加收藏 */
    @Transactional
    public void add(Long userId, Long productId) {
        Long exist = favoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getProductId, productId));
        if (exist > 0) return;
        UserFavorite fav = new UserFavorite();
        fav.setUserId(userId);
        fav.setProductId(productId);
        favoriteMapper.insert(fav);
    }

    /** 取消收藏 */
    @Transactional
    public void remove(Long userId, Long productId) {
        favoriteMapper.delete(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getProductId, productId));
    }

    /** 是否已收藏 */
    public boolean isFavorited(Long userId, Long productId) {
        return favoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getProductId, productId)) > 0;
    }
}
