package com.zhubao.manage.module.shop.controller;

import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.shop.entity.UserAddress;
import com.zhubao.manage.module.shop.service.UserAddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "收货地址")
@RestController
@RequestMapping("/addresses")
@PreAuthorize("isAuthenticated()")
public class AddressController {

    private final UserAddressService addressService;
    private final UserContextHolder userContextHolder;

    public AddressController(UserAddressService addressService, UserContextHolder uch) {
        this.addressService = addressService;
        this.userContextHolder = uch;
    }

    @ApiOperation("获取地址列表")
    @GetMapping
    public ApiResult<List<UserAddress>> list() {
        return ApiResult.ok(addressService.listByUser(userContextHolder.getUserId()));
    }

    @ApiOperation("新增地址")
    @PostMapping
    public ApiResult<UserAddress> create(@RequestBody Map<String, Object> body) {
        UserAddress addr = mapToAddress(body);
        addr.setUserId(userContextHolder.getUserId());
        return ApiResult.ok(addressService.create(addr));
    }

    @ApiOperation("更新地址")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        UserAddress addr = mapToAddress(body);
        addressService.update(id, userContextHolder.getUserId(), addr);
        return ApiResult.ok();
    }

    private UserAddress mapToAddress(Map<String, Object> body) {
        UserAddress a = new UserAddress();
        if (body.containsKey("receiverName")) a.setReceiverName((String) body.get("receiverName"));
        if (body.containsKey("receiverPhone")) a.setReceiverPhone((String) body.get("receiverPhone"));
        if (body.containsKey("province")) a.setProvince((String) body.get("province"));
        if (body.containsKey("city")) a.setCity((String) body.get("city"));
        if (body.containsKey("district")) a.setDistrict((String) body.get("district"));
        if (body.containsKey("detailAddress")) a.setDetailAddress((String) body.get("detailAddress"));
        if (body.containsKey("isDefault")) a.setIsDefault(toInt(body.get("isDefault")));
        return a;
    }

    private Integer toInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof Boolean) return (Boolean) v ? 1 : 0;
        String s = v.toString();
        if ("true".equalsIgnoreCase(s)) return 1;
        if ("false".equalsIgnoreCase(s)) return 0;
        return Integer.valueOf(s);
    }

    @ApiOperation("删除地址")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        addressService.delete(id, userContextHolder.getUserId());
        return ApiResult.ok();
    }

    @ApiOperation("设为默认地址")
    @PutMapping("/{id}/default")
    public ApiResult<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(id, userContextHolder.getUserId());
        return ApiResult.ok();
    }
}
