package com.zhubao.manage.module.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.annotation.OperateLog;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.product.entity.Product;
import com.zhubao.manage.module.product.entity.ProductCategory;
import com.zhubao.manage.module.product.mapper.ProductMapper;
import com.zhubao.manage.module.product.service.ProductCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "商品分类")
@RestController
@RequestMapping("/categories")
public class CategoriesController {

    private final ProductCategoryService productCategoryService;
    private final ProductMapper productMapper;

    public CategoriesController(ProductCategoryService productCategoryService, ProductMapper pm) {
        this.productCategoryService = productCategoryService;
        this.productMapper = pm;
    }

    @ApiOperation("分类树")
    @GetMapping("/tree")
    public ApiResult<List<Map<String, Object>>> tree() {
        List<ProductCategory> categories = productCategoryService.listAll();
        // 父节点：parent_id 为空 或 level=1
        List<ProductCategory> roots = categories.stream()
                .filter(c -> c.getParentId() == null || c.getLevel() == null || c.getLevel() == 1)
                .collect(Collectors.toList());
        List<Map<String, Object>> tree = new ArrayList<>();
        for (ProductCategory root : roots) {
            tree.add(toNode(root, categories));
        }
        return ApiResult.ok(tree);
    }

    private Map<String, Object> toNode(ProductCategory cat, List<ProductCategory> all) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", cat.getId());
        node.put("name", cat.getName());
        node.put("parentId", cat.getParentId());
        node.put("level", cat.getLevel() != null ? cat.getLevel() : 1);
        node.put("sort", cat.getSortOrder() != null ? cat.getSortOrder() : 0);
        node.put("status", "ENABLED".equals(cat.getStatus()) ? "on" : "off");
        node.put("createdAt", cat.getCreatedAt());
        List<ProductCategory> children = all.stream()
                .filter(c -> cat.getId().equals(c.getParentId()))
                .collect(Collectors.toList());
        if (!children.isEmpty()) {
            node.put("children", children.stream().map(c -> toNode(c, all)).collect(Collectors.toList()));
        }
        return node;
    }

    @ApiOperation("分类列表（分页）")
    @GetMapping
    public ApiResult<PageResult<ProductCategory>> list(@Valid PageDTO dto) {
        List<ProductCategory> all = productCategoryService.listAll();
        int start = (int) ((dto.getPageNum() - 1) * dto.getPageSize());
        List<ProductCategory> page = all.stream().skip(start).limit(dto.getPageSize())
                .collect(Collectors.toList());
        return ApiResult.ok(new PageResult<>(dto.getPageNum(), dto.getPageSize(), (long) all.size(), page));
    }

    @PreAuthorize("isAuthenticated()")
    @OperateLog(module = "商品分类", action = "CREATE", targetType = "CATEGORY")
    @ApiOperation("新增分类")
    @PostMapping
    public ApiResult<ProductCategory> create(@Valid @RequestBody ProductCategory category) {
        applyDefaults(category);
        return ApiResult.ok(productCategoryService.create(category));
    }

    @PreAuthorize("isAuthenticated()")
    @OperateLog(module = "商品分类", action = "UPDATE", targetType = "CATEGORY", targetIdExpr = "#id")
    @ApiOperation("修改分类")
    @PutMapping("/{id}")
    public ApiResult<ProductCategory> update(@PathVariable Long id, @Valid @RequestBody ProductCategory category) {
        applyDefaults(category);
        return ApiResult.ok(productCategoryService.update(id, category));
    }

    @PreAuthorize("isAuthenticated()")
    @OperateLog(module = "商品分类", action = "DELETE", targetType = "CATEGORY", targetIdExpr = "#id")
    @ApiOperation("删除分类")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        // 删除一级分类时级联删除其子分类
        productCategoryService.deleteChildren(id);
        productCategoryService.delete(id);
        return ApiResult.ok();
    }

    /** 状态 on/off → ENABLED/DISABLED；根据 parentId 推导 level */
    private void applyDefaults(ProductCategory category) {
        category.setStatus("on".equals(category.getStatus()) ? "ENABLED" : "DISABLED");
        if (category.getParentId() != null && (category.getLevel() == null || category.getLevel() < 2)) {
            category.setLevel(2);
        }
        if (category.getLevel() == null) {
            category.setLevel(1);
        }
    }
}
