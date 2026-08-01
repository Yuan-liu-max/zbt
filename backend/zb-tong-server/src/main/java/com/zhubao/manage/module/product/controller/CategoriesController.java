package com.zhubao.manage.module.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.module.product.entity.Product;
import com.zhubao.manage.module.product.mapper.ProductMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "商品分类")
@RestController
@RequestMapping("/categories")
@PreAuthorize("isAuthenticated()")
public class CategoriesController {

    private final ProductMapper productMapper;

    public CategoriesController(ProductMapper pm) { this.productMapper = pm; }

    @ApiOperation("分类树")
    @GetMapping("/tree")
    public ApiResult<List<Map<String, Object>>> tree() {
        List<Product> all = productMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, Set<String>> catMap = new LinkedHashMap<>();
        for (Product p : all) {
            if (p.getCategory() == null) continue;
            catMap.computeIfAbsent(p.getCategory(), k -> new LinkedHashSet<>());
            if (p.getStyle() != null) catMap.get(p.getCategory()).add(p.getStyle());
        }
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Map.Entry<String, Set<String>> e : catMap.entrySet()) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", e.getKey());
            node.put("name", e.getKey());
            node.put("children", e.getValue().stream().map(s -> {
                Map<String, Object> c = new LinkedHashMap<>();
                c.put("id", s); c.put("name", s); return c;
            }).collect(Collectors.toList()));
            tree.add(node);
        }
        return ApiResult.ok(tree);
    }

    @ApiOperation("分类列表")
    @GetMapping
    public ApiResult<List<Map<String, Object>>> list() {
        List<Product> all = productMapper.selectList(new LambdaQueryWrapper<>());
        return ApiResult.ok(all.stream()
                .filter(p -> p.getCategory() != null)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getCategory()); m.put("name", p.getCategory()); return m;
                })
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(m -> (String) m.get("id"), m -> m, (a, b) -> a, LinkedHashMap::new),
                        m -> new ArrayList<>(m.values()))));
    }
}
