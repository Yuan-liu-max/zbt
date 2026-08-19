package com.zhubao.manage.module.shop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 定位逆地理编码服务 —— 通过高德 Web 服务 API 将经纬度解析为省市区+街道地址。
 *
 * 高德 regeo REST API 不支持浏览器跨域直调，因此由后端代理；
 * 前端浏览器定位（navigator.geolocation）拿到坐标后调用本服务回填地址表单。
 */
@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);
    private static final String AMAP_REGEO_URL = "https://restapi.amap.com/v3/geocode/regeo";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${amap.key:}")
    private String amapKey;

    /**
     * 逆地理编码：经纬度 → 省/市/区 + 详细地址
     *
     * @param lng 经度
     * @param lat 纬度
     * @return { province, city, district, detailAddress, formattedAddress } 或 null（失败）
     */
    public Map<String, Object> regeo(double lng, double lat) {
        if (StringUtils.isBlank(amapKey)) {
            log.warn("高德 API Key 未配置，无法进行逆地理编码");
            return null;
        }
        String url = String.format("%s?location=%s,%s&key=%s&extensions=base&radius=1000",
                AMAP_REGEO_URL, lng, lat, amapKey);
        try {
            String body = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(body);
            if (root == null || !"1".equals(root.path("status").asText())) {
                log.warn("高德 regeo 返回异常: {}", body);
                return null;
            }
            JsonNode regeo = root.path("regeocode");
            JsonNode component = regeo.path("addressComponent");
            JsonNode provinceNode = component.path("province");
            JsonNode cityNode = component.path("city");
            JsonNode districtNode = component.path("district");

            // 直辖市如"北京市"的 city 字段为空，直接用 province
            String province = provinceNode.isTextual() ? provinceNode.asText() : "";
            String city = cityNode.isTextual() && StringUtils.isNotBlank(cityNode.asText()) ? cityNode.asText() : province;
            String district = districtNode.isTextual() ? districtNode.asText() : "";
            // 街道/乡镇（淘宝式布局：省市区+街道一栏，门牌号单独一栏）
            String township = component.path("township").isTextual() ? component.path("township").asText() : "";
            if ("[]".equals(township) || StringUtils.isBlank(township)) township = "";

            // 详细地址：格式化地址依次去掉省/市/区/街道前缀，仅保留门牌号（如"天河路123号"）
            String formatted = regeo.path("formatted_address").asText("");
            String detail = formatted;
            if (StringUtils.isNotBlank(province) && detail.startsWith(province)) detail = detail.substring(province.length());
            if (StringUtils.isNotBlank(city) && detail.startsWith(city)) detail = detail.substring(city.length());
            if (StringUtils.isNotBlank(district) && detail.startsWith(district)) detail = detail.substring(district.length());
            if (StringUtils.isNotBlank(township) && detail.startsWith(township)) detail = detail.substring(township.length());
            // 去掉开头的空格/分隔符
            detail = detail.replaceFirst("^[\\s,，、]+", "");

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("province", province);
            result.put("city", city);
            result.put("district", district);
            result.put("township", township);
            result.put("provinceCode", component.path("province").isTextual() ? component.path("adcode").asText("") : "");
            result.put("cityCode", component.path("citycode").asText(""));
            result.put("districtCode", component.path("adcode").asText(""));
            result.put("detailAddress", detail);
            result.put("formattedAddress", formatted);
            return result;
        } catch (Exception e) {
            log.error("逆地理编码失败 lng={}, lat={}", lng, lat, e);
            return null;
        }
    }
}
