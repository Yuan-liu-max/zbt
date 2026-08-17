package com.zhubao.manage.module.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.annotation.OperateLog;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.product.entity.Product;
import com.zhubao.manage.module.product.entity.ProductBrand;
import com.zhubao.manage.module.product.mapper.ProductMapper;
import com.zhubao.manage.module.product.service.ProductBrandService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "品牌管理")
@RestController
@RequestMapping("/brands")
public class BrandsController {

    private final ProductMapper productMapper;
    private final ProductBrandService productBrandService;

    public BrandsController(ProductMapper pm, ProductBrandService pbs) {
        this.productMapper = pm;
        this.productBrandService = pbs;
    }

    @ApiOperation("品牌分页列表")
    @GetMapping
    public ApiResult<PageResult<Map<String, Object>>> list(@Valid PageDTO dto,
                                                           @RequestParam(required = false) String name,
                                                           @RequestParam(required = false) String status) {
        String statusFilter = StringUtils.isBlank(status) ? null : ("on".equals(status) ? "ENABLED" : "DISABLED");
        IPage<ProductBrand> page = productBrandService.page(dto, name, statusFilter);
        if (page.getTotal() > 0) {
            List<Map<String, Object>> brands = page.getRecords().stream()
                    .map(this::toMap)
                    .collect(Collectors.toList());
            return ApiResult.ok(new PageResult<>(dto.getPageNum(), dto.getPageSize(), page.getTotal(), brands));
        }
        // fallback: derive from Product.style
        List<Product> all = productMapper.selectList(new LambdaQueryWrapper<>());
        List<Map<String, Object>> brands = all.stream()
                .filter(p -> p.getStyle() != null)
                .filter(p -> statusFilter == null || "ENABLED".equals(statusFilter))
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getStyle());
                    m.put("name", p.getStyle());
                    m.put("sort", 0);
                    m.put("status", "on");
                    return m;
                })
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(m -> (String) m.get("id"), m -> m, (a, b) -> a, LinkedHashMap::new),
                        m -> new ArrayList<>(m.values())));

        if (StringUtils.isNotBlank(name)) {
            brands = brands.stream().filter(b -> ((String) b.get("name")).contains(name)).collect(Collectors.toList());
        }

        int start = (int) ((dto.getPageNum() - 1) * dto.getPageSize());
        int end = Math.min(start + (int) dto.getPageSize(), brands.size());
        List<Map<String, Object>> pageList = start < brands.size() ? brands.subList(start, end) : Collections.emptyList();

        return ApiResult.ok(new PageResult<>(dto.getPageNum(), dto.getPageSize(), brands.size(), pageList));
    }

    @ApiOperation("全部品牌")
    @GetMapping("/all")
    public ApiResult<List<Map<String, Object>>> all() {
        List<ProductBrand> list = productBrandService.listAll();
        if (!list.isEmpty()) {
            return ApiResult.ok(list.stream()
                    .map(b -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", b.getId().toString());
                        m.put("name", b.getName());
                        return m;
                    }).collect(Collectors.toList()));
        }
        // fallback: derive from Product.style
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<>());
        return ApiResult.ok(products.stream()
                .filter(p -> p.getStyle() != null)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getStyle());
                    m.put("name", p.getStyle());
                    return m;
                })
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(m -> (String) m.get("id"), m -> m, (a, b) -> a, LinkedHashMap::new),
                        m -> new ArrayList<>(m.values()))));
    }

    @PreAuthorize("isAuthenticated()")
    @OperateLog(module = "品牌管理", action = "CREATE", targetType = "BRAND")
    @ApiOperation("新增品牌")
    @PostMapping
    public ApiResult<ProductBrand> create(@Valid @RequestBody ProductBrand brand) {
        brand.setStatus("on".equals(brand.getStatus()) ? "ENABLED" : "DISABLED");
        return ApiResult.ok(productBrandService.create(brand));
    }

    @PreAuthorize("isAuthenticated()")
    @OperateLog(module = "品牌管理", action = "UPDATE", targetType = "BRAND", targetIdExpr = "#id")
    @ApiOperation("编辑品牌")
    @PutMapping("/{id}")
    public ApiResult<ProductBrand> update(@PathVariable Long id, @Valid @RequestBody ProductBrand brand) {
        brand.setStatus("on".equals(brand.getStatus()) ? "ENABLED" : "DISABLED");
        return ApiResult.ok(productBrandService.update(id, brand));
    }

    @PreAuthorize("isAuthenticated()")
    @OperateLog(module = "品牌管理", action = "DELETE", targetType = "BRAND", targetIdExpr = "#id")
    @ApiOperation("删除品牌")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        productBrandService.delete(id);
        return ApiResult.ok();
    }

    private Map<String, Object> toMap(ProductBrand b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId().toString());
        m.put("name", b.getName());
        m.put("logo", b.getLogo());
        m.put("origin", b.getOrigin());
        m.put("sort", b.getSortOrder() != null ? b.getSortOrder() : 0);
        m.put("status", "ENABLED".equals(b.getStatus()) ? "on" : "off");
        m.put("description", b.getDescription());
        m.put("createdAt", b.getCreatedAt());
        return m;
    }
}
