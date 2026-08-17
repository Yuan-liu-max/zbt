package com.zhubao.manage.module.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.module.product.entity.Product;
import com.zhubao.manage.module.product.entity.ProductImage;
import com.zhubao.manage.module.product.mapper.ProductImageMapper;
import com.zhubao.manage.module.product.mapper.ProductMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "商品图片管理")
@RestController
@RequestMapping("/products/{productId}/images")
public class ProductImageController {

    private final ProductImageMapper productImageMapper;
    private final ProductMapper productMapper;

    public ProductImageController(ProductImageMapper productImageMapper,
                                  ProductMapper productMapper) {
        this.productImageMapper = productImageMapper;
        this.productMapper = productMapper;
    }

    /** 同步 product.image_url 为主图的 URL */
    private void syncPrimary(Long productId) {
        ProductImage primary = productImageMapper.selectOne(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
                        .eq(ProductImage::getIsPrimary, 1)
                        .orderByAsc(ProductImage::getSortOrder)
                        .last("LIMIT 1"));
        String url = (primary != null) ? primary.getImageUrl() : null;
        Product p = productMapper.selectById(productId);
        if (p != null) {
            p.setImageUrl(url);
            productMapper.updateById(p);
        }
    }

    @ApiOperation("获取商品所有图片（按排序号升序）")
    @GetMapping
    public ApiResult<List<ProductImage>> listImages(@PathVariable Long productId) {
        List<ProductImage> list = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
                        .orderByAsc(ProductImage::getSortOrder));
        return ApiResult.ok(list);
    }

    @ApiOperation("新增商品图片")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ApiResult<ProductImage> addImage(@PathVariable Long productId,
                                             @Valid @RequestBody ProductImage img) {
        img.setProductId(productId);
        // 如果设为 1（主图），先清除该商品已有主图标记
        if (img.getIsPrimary() != null && img.getIsPrimary() == 1) {
            ProductImage oldPrimary = productImageMapper.selectOne(
                    new LambdaQueryWrapper<ProductImage>()
                            .eq(ProductImage::getProductId, productId)
                            .eq(ProductImage::getIsPrimary, 1));
            if (oldPrimary != null) {
                oldPrimary.setIsPrimary(0);
                productImageMapper.updateById(oldPrimary);
            }
        }
        productImageMapper.insert(img);
        syncPrimary(productId);
        return ApiResult.ok(img);
    }

    @ApiOperation("更新图片（排序/主图标记）")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ApiResult<ProductImage> updateImage(@PathVariable Long productId,
                                                @PathVariable Long id,
                                                @RequestBody ProductImage img) {
        img.setId(id);
        img.setProductId(productId);
        // 如果设为主图，清除其他主图
        if (img.getIsPrimary() != null && img.getIsPrimary() == 1) {
            ProductImage oldPrimary = productImageMapper.selectOne(
                    new LambdaQueryWrapper<ProductImage>()
                            .eq(ProductImage::getProductId, productId)
                            .eq(ProductImage::getIsPrimary, 1)
                            .ne(ProductImage::getId, id));
            if (oldPrimary != null) {
                oldPrimary.setIsPrimary(0);
                productImageMapper.updateById(oldPrimary);
            }
        }
        productImageMapper.updateById(img);
        syncPrimary(productId);
        return ApiResult.ok(productImageMapper.selectById(id));
    }

    @ApiOperation("删除商品图片")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ApiResult<Void> deleteImage(@PathVariable Long productId, @PathVariable Long id) {
        ProductImage img = productImageMapper.selectById(id);
        if (img == null || !img.getProductId().equals(productId)) {
            return ApiResult.fail("图片不存在");
        }
        productImageMapper.deleteById(id);
        syncPrimary(productId);
        return ApiResult.ok();
    }
}
