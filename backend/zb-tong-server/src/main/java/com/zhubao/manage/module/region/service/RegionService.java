package com.zhubao.manage.module.region.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.module.region.entity.Region;
import com.zhubao.manage.module.region.mapper.RegionMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 行政区域字典服务
 *
 * 职责：
 * 1. 启动时通过高德 district API 自动同步省市区三级字典（region_dict 表）
 * 2. 提供省市区树 / area-list（前端三级联动数据源）
 * 3. 地址校验清洗：提交的省市区必须在字典表中且层级关系正确，过滤无效地址
 */
@Service
public class RegionService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RegionService.class);
    private static final String AMAP_DISTRICT_URL = "https://restapi.amap.com/v3/config/district";

    private final RegionMapper regionMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${amap.key:}")
    private String amapKey;

    public RegionService(RegionMapper regionMapper) {
        this.regionMapper = regionMapper;
    }

    // ==================== 启动自动同步 ====================

    @Override
    public void run(ApplicationArguments args) {
        try {
            long count = regionMapper.selectCount(null);
            if (count > 0) {
                log.info("行政区域字典已存在 {} 条，跳过同步", count);
                return;
            }
            syncFromAmap();
        } catch (Exception e) {
            // 同步失败不阻塞启动，地址校验时会提示字典未初始化
            log.warn("行政区域字典自动同步失败（可稍后手动同步）: {}", e.getMessage());
        }
    }

    /**
     * 全量同步省市区（高德 district API，subdistrict=3 一次取全量）
     */
    public synchronized int syncFromAmap() {
        if (StringUtils.isBlank(amapKey)) {
            throw new BusinessException(500, "高德 Key 未配置，无法同步行政区字典");
        }
        String url = String.format("%s?keywords=%s&subdistrict=3&extensions=base&key=%s",
                AMAP_DISTRICT_URL, urlEncode("中国"), amapKey);
        try {
            // 必须传 URI 对象而非 String：RestTemplate 对 String 会二次编码 %XX，导致 keywords 失效
            String body = restTemplate.getForObject(java.net.URI.create(url), String.class);
            JsonNode root = objectMapper.readTree(body);
            if (root == null || !"1".equals(root.path("status").asText())) {
                throw new BusinessException(500, "高德行政区接口返回异常: " + body);
            }
            JsonNode districts = root.path("districts");
            if (!districts.isArray() || districts.size() == 0) {
                log.error("高德行政区接口返回空数据: {}", body);
                throw new BusinessException(500, "高德行政区接口返回空数据");
            }
            JsonNode country = districts.get(0);
            List<Region> all = new ArrayList<>();
            java.util.Set<String> seenCodes = new java.util.HashSet<>();
            int sort = 0;
            for (JsonNode provNode : country.path("districts")) {
                String provCode = provNode.path("adcode").asText();
                String provName = provNode.path("name").asText();
                if (seenCodes.add(provCode)) {
                    all.add(buildRegion(provCode, provName, "province", null, sort++));
                }

                for (JsonNode cityNode : provNode.path("districts")) {
                    String cityCode = cityNode.path("adcode").asText();
                    String cityName = cityNode.path("name").asText();
                    // 省直辖县级市（如济源 419001）city 与 district 同 adcode，跳过重复
                    if (seenCodes.add(cityCode)) {
                        all.add(buildRegion(cityCode, cityName, "city", provCode, sort++));
                    }

                    for (JsonNode distNode : cityNode.path("districts")) {
                        String distCode = distNode.path("adcode").asText();
                        String distName = distNode.path("name").asText();
                        if (seenCodes.add(distCode)) {
                            all.add(buildRegion(distCode, distName, "district", cityCode, sort++));
                        }
                    }
                }
            }
            if (all.isEmpty()) throw new BusinessException(500, "行政区同步结果为空");
            // 全量替换（简单可靠：清空后插入，保证与高德一致）
            regionMapper.delete(null);
            for (Region r : all) regionMapper.insert(r);
            log.info("行政区域字典同步完成: 共 {} 条", all.size());
            return all.size();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("行政区同步失败", e);
            throw new BusinessException(500, "行政区同步失败: " + e.getMessage());
        }
    }

    private Region buildRegion(String code, String name, String level, String parentCode, int sort) {
        Region r = new Region();
        r.setCode(code);
        r.setName(name);
        r.setLevel(level);
        r.setParentCode(parentCode);
        r.setSort(sort);
        return r;
    }

    // ==================== 查询 ====================

    /** 省市区树（前端三级联动数据源） */
    public List<Map<String, Object>> tree() {
        List<Region> all = regionMapper.selectList(
                new LambdaQueryWrapper<Region>().orderByAsc(Region::getSort));
        Map<String, Map<String, Object>> districtMap = new LinkedHashMap<>();
        Map<String, Map<String, Object>> cityMap = new LinkedHashMap<>();
        List<Map<String, Object>> provinceList = new ArrayList<>();

        for (Region r : all) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("code", r.getCode());
            node.put("name", r.getName());
            node.put("level", r.getLevel());
            node.put("children", new ArrayList<Map<String, Object>>());
            if ("district".equals(r.getLevel())) districtMap.put(r.getCode(), node);
            else if ("city".equals(r.getLevel())) cityMap.put(r.getCode(), node);
            else provinceList.add(node);
        }
        // 区挂到市
        for (Region r : all) {
            if (!"district".equals(r.getLevel())) continue;
            Map<String, Object> node = districtMap.get(r.getCode());
            Map<String, Object> city = cityMap.get(r.getParentCode());
            if (city != null) ((List<Map<String, Object>>) city.get("children")).add(node);
        }
        // 市挂到省
        for (Region r : all) {
            if (!"city".equals(r.getLevel())) continue;
            Map<String, Object> node = cityMap.get(r.getCode());
            Map<String, Object> prov = provinceList.stream()
                    .filter(p -> p.get("code").equals(r.getParentCode())).findFirst().orElse(null);
            if (prov != null) ((List<Map<String, Object>>) prov.get("children")).add(node);
        }
        return provinceList;
    }

    /** 供 Vant Area 组件的 area-list 格式（编码->名称扁平 Map） */
    public Map<String, Map<String, String>> areaList() {
        List<Region> all = regionMapper.selectList(null);
        Map<String, String> provinceList = new LinkedHashMap<>();
        Map<String, String> cityList = new LinkedHashMap<>();
        Map<String, String> countyList = new LinkedHashMap<>();
        for (Region r : all) {
            if ("province".equals(r.getLevel())) provinceList.put(r.getCode(), r.getName());
            else if ("city".equals(r.getLevel())) cityList.put(r.getCode(), r.getName());
            else if ("district".equals(r.getLevel())) countyList.put(r.getCode(), r.getName());
        }
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        result.put("province_list", provinceList);
        result.put("city_list", cityList);
        result.put("county_list", countyList);
        return result;
    }

    // ==================== 街道查询（实时调高德，不落库） ====================

    /**
     * 按区县编码查询街道/乡镇列表（淘宝式：选中省市区后街道可下拉选择）。
     * 街道数据量大且变动频繁，实时从高德 district API 拉取（区县 → 街道）。
     */
    public List<Map<String, String>> streets(String districtCode) {
        if (StringUtils.isBlank(amapKey)) {
            throw new BusinessException(500, "高德 Key 未配置，无法查询街道");
        }
        if (StringUtils.isBlank(districtCode)) {
            throw new BusinessException(400, "请先选择区县");
        }
        String url = String.format("%s?keywords=%s&subdistrict=1&extensions=base&key=%s",
                AMAP_DISTRICT_URL, districtCode, amapKey);
        try {
            String body = restTemplate.getForObject(java.net.URI.create(url), String.class);
            JsonNode root = objectMapper.readTree(body);
            if (root == null || !"1".equals(root.path("status").asText())
                    || !root.path("districts").isArray() || root.path("districts").size() == 0) {
                throw new BusinessException(500, "高德街道接口返回异常");
            }
            JsonNode district = root.path("districts").get(0);
            List<Map<String, String>> streets = new ArrayList<>();
            for (JsonNode streetNode : district.path("districts")) {
                if (!"street".equals(streetNode.path("level").asText())) continue;
                Map<String, String> s = new LinkedHashMap<>();
                s.put("name", streetNode.path("name").asText());
                s.put("code", streetNode.path("adcode").asText());
                streets.add(s);
            }
            return streets;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询街道失败 districtCode={}", districtCode, e);
            throw new BusinessException(500, "查询街道失败: " + e.getMessage());
        }
    }

    // ==================== 地址校验清洗 ====================

    /**
     * 校验并清洗省市区三级地址。
     *
     * @param province 省名
     * @param city     市名
     * @param district 区名
     * @param detailAddress 详细地址（楼栋/门牌）
     * @return 清洗后的 { province, city, district, provinceCode, cityCode, districtCode, detailAddress }
     * @throws BusinessException 地址无效时抛出
     */
    public Map<String, String> validateAndClean(String province, String city, String district, String detailAddress) {
        // 1. 详细地址必须手动补充楼栋/门牌（不允许只填 POI 名/区域名）
        String detail = detailAddress == null ? "" : detailAddress.trim();
        if (detail.length() < 5) {
            throw new BusinessException(400, "详细地址请补充楼栋/门牌号（至少5个字符）");
        }
        if (!detail.matches(".*[0-9０-９].*")) {
            throw new BusinessException(400, "详细地址请补充门牌号（需包含数字）");
        }

        // 2. 三级区域必须完整
        if (StringUtils.isBlank(province) || StringUtils.isBlank(city) || StringUtils.isBlank(district)) {
            throw new BusinessException(400, "请选择完整的省/市/区");
        }
        String pName = province.trim();
        String cName = city.trim();
        String dName = district.trim();

        Region dist = regionMapper.selectOne(new LambdaQueryWrapper<Region>()
                .eq(Region::getLevel, "district").eq(Region::getName, dName));
        if (dist == null) {
            throw new BusinessException(400, "区县「" + dName + "」不在行政区字典中，请重新选择");
        }
        Region cityRegion = regionMapper.selectById(dist.getParentCode());
        if (cityRegion == null || !"city".equals(cityRegion.getLevel())) {
            throw new BusinessException(400, "区县「" + dName + "」与市「" + cName + "」不匹配，请重新选择");
        }
        // 直辖市场景：省=市（如北京市），city 层名为"北京城区"，允许市名=省名
        boolean cityNameMatch = cityRegion.getName().equals(cName)
                || cName.endsWith(cityRegion.getName())
                || cName.equals(pName);
        if (!cityNameMatch) {
            throw new BusinessException(400, "区县「" + dName + "」与市「" + cName + "」不匹配，请重新选择");
        }
        Region prov = regionMapper.selectById(cityRegion.getParentCode());
        if (prov == null || !"province".equals(prov.getLevel())
                || !prov.getName().equals(pName) && !pName.endsWith(prov.getName())) {
            throw new BusinessException(400, "市「" + cName + "」与省「" + pName + "」不匹配，请重新选择");
        }

        // 3. 返回清洗后的标准名称（以字典为准）
        // 直辖市归一：city 显示省名（"北京城区" → "北京市"），层级编码不变
        boolean municipality = cityRegion.getName().endsWith("城区");
        Map<String, String> cleaned = new LinkedHashMap<>();
        cleaned.put("province", prov.getName());
        cleaned.put("city", municipality ? prov.getName() : cityRegion.getName());
        cleaned.put("district", dist.getName());
        cleaned.put("provinceCode", prov.getCode());
        cleaned.put("cityCode", cityRegion.getCode());
        cleaned.put("districtCode", dist.getCode());
        cleaned.put("detailAddress", detail);
        return cleaned;
    }

    private String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }
}
