package com.zhubao.manage.module.shop.controller;

import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.shop.entity.UserAddress;
import com.zhubao.manage.module.shop.service.LocationService;
import com.zhubao.manage.module.shop.service.UserAddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Api(tags = "收货地址")
@RestController
@RequestMapping("/addresses")
@PreAuthorize("isAuthenticated()")
public class AddressController {

    private final UserAddressService addressService;
    private final LocationService locationService;
    private final com.zhubao.manage.module.region.service.RegionService regionService;
    private final UserContextHolder userContextHolder;

    public AddressController(UserAddressService addressService, LocationService locationService,
                             com.zhubao.manage.module.region.service.RegionService regionService,
                             UserContextHolder uch) {
        this.addressService = addressService;
        this.locationService = locationService;
        this.regionService = regionService;
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
        UserAddress addr = mapAndValidate(body);
        addr.setUserId(userContextHolder.getUserId());
        return ApiResult.ok(addressService.create(addr));
    }

    @ApiOperation("更新地址")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        UserAddress addr = mapAndValidate(body);
        addressService.update(id, userContextHolder.getUserId(), addr);
        return ApiResult.ok();
    }

    /**
     * 解析请求体并做地址校验清洗（省市区字典校验 + 详细地址门牌校验）。
     * 校验失败抛 BusinessException，由全局异常处理器返回错误信息。
     */
    private UserAddress mapAndValidate(Map<String, Object> body) {
        String province = body.get("province") == null ? null : body.get("province").toString();
        String city = body.get("city") == null ? null : body.get("city").toString();
        String district = body.get("district") == null ? null : body.get("district").toString();
        String street = body.get("street") == null ? null : body.get("street").toString();
        String detailAddress = body.get("detailAddress") == null ? null : body.get("detailAddress").toString();

        // 校验并清洗（以行政区字典为准，自动归一标准名称）
        Map<String, String> cleaned = regionService.validateAndClean(province, city, district, detailAddress);

        UserAddress a = new UserAddress();
        if (body.containsKey("receiverName")) a.setReceiverName((String) body.get("receiverName"));
        if (body.containsKey("receiverPhone")) a.setReceiverPhone((String) body.get("receiverPhone"));
        a.setProvince(cleaned.get("province"));
        a.setCity(cleaned.get("city"));
        a.setDistrict(cleaned.get("district"));
        a.setStreet(street == null ? null : street.trim());
        a.setDetailAddress(cleaned.get("detailAddress"));
        if (body.containsKey("longitude") && body.get("longitude") != null) a.setLongitude(toDecimal(body.get("longitude")));
        if (body.containsKey("latitude") && body.get("latitude") != null) a.setLatitude(toDecimal(body.get("latitude")));
        if (body.containsKey("isDefault")) a.setIsDefault(toInt(body.get("isDefault")));
        return a;
    }

    @ApiOperation("逆地理编码：经纬度 → 省市区+详细地址（新增地址定位）")
    @GetMapping("/regeo")
    public ApiResult<Map<String, Object>> regeo(@RequestParam double lng, @RequestParam double lat) {
        Map<String, Object> result = locationService.regeo(lng, lat);
        if (result == null) return ApiResult.fail("定位解析失败，请检查高德 Key 配置或稍后重试");
        return ApiResult.ok(result);
    }

    private BigDecimal toDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return null; }
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
