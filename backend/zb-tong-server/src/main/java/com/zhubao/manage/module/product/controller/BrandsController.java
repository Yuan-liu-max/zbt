package com.zhubao.manage.module.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.product.entity.Product;
import com.zhubao.manage.module.product.mapper.ProductMapper;
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
@PreAuthorize("isAuthenticated()")
public class BrandsController {

    private final ProductMapper productMapper;

    public BrandsController(ProductMapper pm) { this.productMapper = pm; }

    @ApiOperation("品牌分页列表")
    @GetMapping
    public ApiResult<PageResult<Map<String, Object>>> list(@Valid PageDTO dto, @RequestParam(required = false) String name) {
        List<Product> all = productMapper.selectList(new LambdaQueryWrapper<>());
        List<Map<String, Object>> brands = all.stream()
                .filter(p -> p.getStyle() != null)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getStyle()); m.put("name", p.getStyle()); return m;
                })
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(m -> (String) m.get("id"), m -> m, (a, b) -> a, LinkedHashMap::new),
                        m -> new ArrayList<>(m.values())));

        if (StringUtils.isNotBlank(name)) {
            brands = brands.stream().filter(b -> ((String) b.get("name")).contains(name)).collect(Collectors.toList());
        }

        int start = (int) ((dto.getPageNum() - 1) * dto.getPageSize());
        int end = Math.min(start + (int) dto.getPageSize(), brands.size());
        List<Map<String, Object>> page = start < brands.size() ? brands.subList(start, end) : Collections.emptyList();

        PageResult<Map<String, Object>> r = new PageResult<>(dto.getPageNum(), dto.getPageSize(), brands.size(), page);
        return ApiResult.ok(r);
    }

    @ApiOperation("全部品牌")
    @GetMapping("/all")
    public ApiResult<List<Map<String, Object>>> all() {
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<>());
        return ApiResult.ok(products.stream()
                .filter(p -> p.getStyle() != null)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getStyle()); m.put("name", p.getStyle()); return m;
                })
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(m -> (String) m.get("id"), m -> m, (a, b) -> a, LinkedHashMap::new),
                        m -> new ArrayList<>(m.values()))));
    }
}
