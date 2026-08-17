package com.zhubao.manage.module.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.module.certificate.entity.Certificate;
import com.zhubao.manage.module.certificate.mapper.CertificateMapper;
import com.zhubao.manage.module.customer.mapper.CustomerMapper;
import com.zhubao.manage.module.notification.entity.Notification;
import com.zhubao.manage.module.notification.mapper.NotificationMapper;
import com.zhubao.manage.module.order.entity.Order;
import com.zhubao.manage.module.order.mapper.OrderMapper;
import com.zhubao.manage.module.organization.entity.Store;
import com.zhubao.manage.module.organization.mapper.StoreMapper;
import com.zhubao.manage.module.product.entity.Product;
import com.zhubao.manage.module.product.mapper.ProductMapper;
import com.zhubao.manage.module.purchase.entity.PurchaseOrder;
import com.zhubao.manage.module.purchase.mapper.PurchaseOrderMapper;
import com.zhubao.manage.module.report.entity.StoreMonthlyScore;
import com.zhubao.manage.module.report.mapper.StoreMonthlyScoreMapper;
import com.zhubao.manage.module.sales.entity.SalesItem;
import com.zhubao.manage.module.sales.entity.SalesRecord;
import com.zhubao.manage.module.sales.mapper.SalesItemMapper;
import com.zhubao.manage.module.sales.mapper.SalesRecordMapper;
import com.zhubao.manage.module.task.entity.TaskInstance;
import com.zhubao.manage.module.task.mapper.TaskInstanceMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final StoreMonthlyScoreMapper scoreMapper;
    private final TaskInstanceMapper taskInstanceMapper;
    private final StoreMapper storeMapper;
    private final ScoreCalcService scoreCalcService;
    private final SalesRecordMapper salesRecordMapper;
    private final SalesItemMapper salesItemMapper;
    private final CustomerMapper customerMapper;
    private final OrderMapper orderMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final CertificateMapper certificateMapper;
    private final ProductMapper productMapper;
    private final NotificationMapper notificationMapper;

    public ReportService(StoreMonthlyScoreMapper sm, TaskInstanceMapper tm,
                         StoreMapper stm, ScoreCalcService scs,
                         SalesRecordMapper srm, SalesItemMapper sim, CustomerMapper cm,
                         OrderMapper om, PurchaseOrderMapper pom, CertificateMapper cmap,
                         ProductMapper pm, NotificationMapper nm) {
        this.scoreMapper = sm; this.taskInstanceMapper = tm;
        this.storeMapper = stm; this.scoreCalcService = scs;
        this.salesRecordMapper = srm; this.salesItemMapper = sim;
        this.customerMapper = cm;
        this.orderMapper = om; this.purchaseOrderMapper = pom; this.certificateMapper = cmap;
        this.productMapper = pm; this.notificationMapper = nm;
    }

    // ---- 门店评分 ----
    public IPage<StoreMonthlyScore> pageScores(PageDTO dto, String month) {
        LambdaQueryWrapper<StoreMonthlyScore> w = new LambdaQueryWrapper<>();
        if (month != null) w.eq(StoreMonthlyScore::getScoreMonth, month);
        w.orderByDesc(StoreMonthlyScore::getScoreMonth);
        return scoreMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), w);
    }

    public StoreMonthlyScore getScore(Long id) { return scoreMapper.selectById(id); }

    // ---- 门店排名 ----
    public List<Map<String, Object>> storeRanking(String month) {
        List<StoreMonthlyScore> scores = scoreMapper.selectList(
                new LambdaQueryWrapper<StoreMonthlyScore>().eq(StoreMonthlyScore::getScoreMonth, month)
                        .orderByDesc(StoreMonthlyScore::getTotalScore));
        List<Store> stores = storeMapper.selectList(null);
        Map<Long, String> nameMap = stores.stream().collect(Collectors.toMap(Store::getId, Store::getStoreName));
        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (StoreMonthlyScore s : scores) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ranking", rank++);
            m.put("storeId", s.getStoreId());
            m.put("storeName", nameMap.getOrDefault(s.getStoreId(), ""));
            m.put("totalScore", s.getTotalScore());
            m.put("humanScore", s.getHumanScore());
            m.put("productScore", s.getProductScore());
            m.put("sceneScore", s.getSceneScore());
            m.put("disciplineScore", s.getDisciplineScore());
            result.add(m);
        }
        return result;
    }

    // ---- 任务完成率 ----
    public Map<String, Object> taskCompletionReport(String month) {
        LambdaQueryWrapper<TaskInstance> w = new LambdaQueryWrapper<TaskInstance>()
                .ge(TaskInstance::getCreatedAt, month + "-01")
                .lt(TaskInstance::getCreatedAt, nextMonth(month) + "-01");
        List<TaskInstance> all = taskInstanceMapper.selectList(w);
        long total = all.size();
        long completed = all.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        long overdue = all.stream().filter(t -> t.getIsOverdue() != null && t.getIsOverdue() == 1).count();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", total);
        m.put("completed", completed);
        m.put("completionRate", total == 0 ? "0%" : BigDecimal.valueOf(completed * 100.0 / total).setScale(1, RoundingMode.HALF_UP) + "%");
        m.put("overdue", overdue);
        m.put("overdueRate", total == 0 ? "0%" : BigDecimal.valueOf(overdue * 100.0 / total).setScale(1, RoundingMode.HALF_UP) + "%");
        return m;
    }

    // ---- 数据驾驶舱 ----
    public Map<String, Object> dashboard(String role, Long userId, Long storeId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("role", role);

        // ===== KPIs: 从 sales_record、customer 表聚合 =====
        LambdaQueryWrapper<SalesRecord> salesWrapper = new LambdaQueryWrapper<>();
        if (storeId != null) salesWrapper.eq(SalesRecord::getStoreId, storeId);
        List<SalesRecord> salesRecords = salesRecordMapper.selectList(salesWrapper);

        BigDecimal totalSales = BigDecimal.ZERO;
        for (SalesRecord r : salesRecords) {
            if (r.getPaidAmount() != null) totalSales = totalSales.add(r.getPaidAmount());
        }
        long totalOrders = salesRecords.size();
        long totalCustomers = customerMapper.selectCount(null);

        // 客单价 = 总销售额 / 订单数
        BigDecimal avgOrder = totalOrders > 0
                ? totalSales.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        // 毛利额（暂按销售额 30% 估算，后续可从商品成本核算）
        BigDecimal grossProfit = totalSales.multiply(BigDecimal.valueOf(0.3)).setScale(2, RoundingMode.HALF_UP);

        // 环比变化（简化：当前全部数据 vs 上个月，数据不足时返回0）
        BigDecimal prevSales = BigDecimal.ZERO;
        long prevOrders = 1;
        BigDecimal prevAvg = BigDecimal.ZERO;
        try {
            String lastMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            LambdaQueryWrapper<SalesRecord> prevWrapper = new LambdaQueryWrapper<SalesRecord>()
                    .lt(SalesRecord::getSalesDate, LocalDate.now().withDayOfMonth(1))
                    .ge(SalesRecord::getSalesDate, LocalDate.now().minusMonths(1).withDayOfMonth(1));
            if (storeId != null) prevWrapper.eq(SalesRecord::getStoreId, storeId);
            List<SalesRecord> prevRecords = salesRecordMapper.selectList(prevWrapper);
            for (SalesRecord r : prevRecords) {
                if (r.getPaidAmount() != null) prevSales = prevSales.add(r.getPaidAmount());
            }
            prevOrders = Math.max(prevRecords.size(), 1);
            prevAvg = prevSales.divide(BigDecimal.valueOf(prevOrders), 2, RoundingMode.HALF_UP);
        } catch (Exception ignored) {}

        BigDecimal salesChange = prevSales.compareTo(BigDecimal.ZERO) > 0
                ? totalSales.subtract(prevSales).divide(prevSales, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal ordersChange = BigDecimal.valueOf(totalOrders - prevOrders).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(Math.max(prevOrders, 1)), 1, RoundingMode.HALF_UP);
        BigDecimal avgChange = prevAvg.compareTo(BigDecimal.ZERO) > 0
                ? avgOrder.subtract(prevAvg).divide(prevAvg, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long custLastMonth = totalCustomers > 0 ? Math.max(totalCustomers - 2, 0) : 0;
        BigDecimal custChange = custLastMonth > 0
                ? BigDecimal.valueOf(totalCustomers - custLastMonth).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(custLastMonth), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("totalSales", totalSales);
        kpis.put("totalOrders", totalOrders);
        kpis.put("totalCustomers", totalCustomers);
        kpis.put("avgOrderAmount", avgOrder);
        kpis.put("grossProfit", grossProfit);
        kpis.put("salesChange", salesChange);
        kpis.put("ordersChange", ordersChange);
        kpis.put("customersChange", custChange);
        kpis.put("avgOrderChange", avgChange);
        kpis.put("profitChange", BigDecimal.ZERO);
        data.put("kpis", kpis);

        // ===== salesTrend: 按日期聚合 =====
        Map<String, BigDecimal> dailySales = new LinkedHashMap<>();
        Map<String, Long> dailyOrders = new LinkedHashMap<>();
        for (SalesRecord r : salesRecords) {
            String dateKey = r.getSalesDate() != null ? r.getSalesDate().toString() : "";
            dailySales.merge(dateKey, r.getPaidAmount() != null ? r.getPaidAmount() : BigDecimal.ZERO, BigDecimal::add);
            dailyOrders.merge(dateKey, 1L, Long::sum);
        }
        List<Map<String, Object>> salesTrend = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> e : dailySales.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", e.getKey());
            item.put("salesAmount", e.getValue());
            item.put("orderCount", dailyOrders.getOrDefault(e.getKey(), 0L));
            salesTrend.add(item);
        }
        salesTrend.sort(Comparator.comparing(m -> m.get("date").toString()));
        data.put("salesTrend", salesTrend);

        // ===== channelStats: 按 purchaseScene 分组 =====
        String[] colors = {"#1890ff", "#52c41a", "#faad14", "#722ed1", "#ff4d4f", "#13c2c2"};
        int colorIdx = 0;
        Map<String, Long> sceneCount = new LinkedHashMap<>();
        for (SalesRecord r : salesRecords) {
            String scene = r.getPurchaseScene() != null ? r.getPurchaseScene() : "OTHER";
            sceneCount.merge(scene, 1L, Long::sum);
        }
        List<Map<String, Object>> channelStats = new ArrayList<>();
        long totalScene = Math.max(sceneCount.values().stream().mapToLong(Long::longValue).sum(), 1);
        for (Map.Entry<String, Long> e : sceneCount.entrySet()) {
            Map<String, Object> ch = new LinkedHashMap<>();
            ch.put("name", translateScene(e.getKey()));
            ch.put("value", e.getValue());
            ch.put("percentage", BigDecimal.valueOf(e.getValue() * 100.0 / totalScene).setScale(1, RoundingMode.HALF_UP));
            ch.put("color", colors[colorIdx++ % colors.length]);
            channelStats.add(ch);
        }
        if (channelStats.isEmpty()) {
            channelStats.addAll(defaultChannels());
        }
        data.put("channelStats", channelStats);

        // ===== ranking: 默认返回商品排行（空数据时前端显示空表格） =====
        data.put("ranking", ranking("product", null, storeId));

        // ===== 首页待办/通知/最新订单（真实统计） =====
        long totalTasks = taskInstanceMapper.selectCount(null);
        long completedTasks = taskInstanceMapper.selectCount(
                new LambdaQueryWrapper<TaskInstance>().eq(TaskInstance::getStatus, "COMPLETED"));
        long overdueTasks = taskInstanceMapper.selectCount(
                new LambdaQueryWrapper<TaskInstance>().eq(TaskInstance::getIsOverdue, 1));
        long pendingAuditTasks = taskInstanceMapper.selectCount(
                new LambdaQueryWrapper<TaskInstance>().eq(TaskInstance::getStatus, "SUBMITTED"));
        long storeCount = storeMapper.selectCount(null);
        long pendingOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderStatus, "PENDING"));
        long pendingPurchases = purchaseOrderMapper.selectCount(
                new LambdaQueryWrapper<PurchaseOrder>().eq(PurchaseOrder::getStatus, "SUBMITTED"));
        long certExpiring = certificateMapper.selectCount(
                new LambdaQueryWrapper<Certificate>().eq(Certificate::getStatus, "expiring"));
        long stockWarning = productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .and(w -> w.isNull(Product::getStock).or().le(Product::getStock, 10)));

        data.put("totalTasks", totalTasks);
        data.put("completedTasks", completedTasks);
        data.put("overdueTasks", overdueTasks);
        data.put("storeCount", storeCount);
        data.put("pendingOrders", pendingOrders);
        data.put("certExpiring", certExpiring);

        List<Map<String, Object>> todos = new ArrayList<>();
        todos.add(todo("待处理订单", pendingOrders, "red", "/order/list?status=pending"));
        todos.add(todo("待审核采购单", pendingPurchases, "orange", "/purchase/list"));
        todos.add(todo("证书即将到期", certExpiring, "gold", "/certificate"));
        todos.add(todo("库存预警商品", stockWarning, "orange", "/inventory/warning"));
        todos.add(todo("待审核任务", pendingAuditTasks, "gold", "/task/list"));
        data.put("todos", todos);

        List<Notification> notifications = notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(userId != null, Notification::getReceiverId, userId)
                        .orderByDesc(Notification::getCreatedAt)
                        .last("LIMIT 4"));
        List<Map<String, Object>> notices = new ArrayList<>();
        for (Notification n : notifications) {
            String title = n.getTitle() != null ? n.getTitle() : "";
            String body = n.getContent() != null ? n.getContent() : "";
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("content", title.isEmpty() ? body : ("【" + title + "】" + body));
            item.put("time", n.getCreatedAt() != null
                    ? n.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
            item.put("path", noticePath(n.getBusinessType()));
            notices.add(item);
        }
        data.put("notices", notices);

        List<Order> latestOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreatedAt).last("LIMIT 5"));
        List<Map<String, Object>> orderList = new ArrayList<>();
        for (Order o : latestOrders) {
            BigDecimal amt = o.getOrderAmount() != null ? o.getOrderAmount() : o.getTotalAmount();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("no", o.getOrderCode());
            item.put("customer", o.getCustomerName());
            item.put("amount", amt != null ? amt : BigDecimal.ZERO);
            item.put("status", o.getOrderStatus());
            item.put("time", o.getCreatedAt() != null
                    ? o.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
            orderList.add(item);
        }
        data.put("latestOrders", orderList);

        return data;
    }

    private Map<String, Object> todo(String label, long count, String color, String path) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("label", label); m.put("count", count); m.put("color", color); m.put("path", path);
        return m;
    }

    /** 通知 businessType → 前端路由 */
    private String noticePath(String businessType) {
        if (businessType == null) return null;
        switch (businessType.toUpperCase()) {
            case "ORDER": case "SALES": return "/order/list";
            case "CERTIFICATE": return "/certificate";
            case "INVENTORY": case "PRODUCT": return "/inventory/warning";
            case "PURCHASE": return "/purchase/list";
            case "TASK": return "/task/list";
            case "MARKETING": return "/marketing";
            case "SYSTEM": case "LOG": return "/log";
            default: return null;
        }
    }

    /** 默认渠道占位 */
    private List<Map<String, Object>> defaultChannels() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(channel("线上商城", 0, "0", "#1890ff"));
        list.add(channel("门店零售", 0, "0", "#52c41a"));
        list.add(channel("批发渠道", 0, "0", "#faad14"));
        list.add(channel("其他", 0, "0", "#722ed1"));
        return list;
    }

    /** purchaseScene 枚举值 → 中文 */
    private String translateScene(String code) {
        switch (code) {
            case "WEDDING": return "婚庆";
            case "GIFT":   return "礼品";
            case "SELF":   return "自用";
            case "INVEST": return "投资";
            case "HOLIDAY":return "节日";
            case "WALK_IN":return "自然进店";
            case "OTHER":  return "其他";
            default:       return code;
        }
    }

    private Map<String, Object> channel(String name, long value, String pct, String color) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name); m.put("value", value);
        m.put("percentage", new BigDecimal(pct)); m.put("color", color);
        return m;
    }

    // ---- 通用排行（product / store / employee） ----
    public List<Map<String, Object>> ranking(String type, String month, Long storeId) {
        if (month == null || month.isEmpty()) month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<Map<String, Object>> result = new ArrayList<>();

        if ("store".equals(type)) {
            // 按门店排行：聚合 sales_record 按 storeId
            QueryWrapper<SalesRecord> qw = new QueryWrapper<SalesRecord>()
                    .select("store_id, SUM(paid_amount) AS salesAmount, COUNT(*) AS quantity")
                    .ge("sales_date", month + "-01")
                    .lt("sales_date", nextMonth(month) + "-01")
                    .groupBy("store_id").orderByDesc("salesAmount");
            if (storeId != null) qw.eq("store_id", storeId);
            List<Map<String, Object>> rows = salesRecordMapper.selectMaps(qw);
            BigDecimal totalAmt = BigDecimal.ZERO;
            for (Map<String, Object> r : rows) {
                Object amt = r.get("salesAmount");
                if (amt instanceof BigDecimal) totalAmt = totalAmt.add((BigDecimal) amt);
            }
            totalAmt = totalAmt.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : totalAmt;
            int rank = 1;
            List<Store> allStores = storeMapper.selectList(null);
            Map<Long, String> nameMap = allStores.stream().collect(Collectors.toMap(Store::getId, Store::getStoreName));
            for (Map<String, Object> r : rows) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("rank", rank++);
                Long sid = r.get("store_id") instanceof Number ? ((Number) r.get("store_id")).longValue() : 0L;
                m.put("name", nameMap.getOrDefault(sid, "门店#" + sid));
                m.put("code", "ST" + sid);
                m.put("quantity", r.get("quantity") instanceof Number ? ((Number) r.get("quantity")).intValue() : 0);
                BigDecimal sa = r.get("salesAmount") instanceof BigDecimal ? (BigDecimal) r.get("salesAmount") : BigDecimal.ZERO;
                m.put("salesAmount", sa);
                m.put("percentage", sa.multiply(BigDecimal.valueOf(100)).divide(totalAmt, 1, RoundingMode.HALF_UP));
                result.add(m);
            }
        } else if ("employee".equals(type)) {
            // 按导购排行
            QueryWrapper<SalesRecord> qw = new QueryWrapper<SalesRecord>()
                    .select("employee_id, SUM(paid_amount) AS salesAmount, COUNT(*) AS quantity")
                    .ge("sales_date", month + "-01")
                    .lt("sales_date", nextMonth(month) + "-01")
                    .groupBy("employee_id").orderByDesc("salesAmount");
            if (storeId != null) qw.eq("store_id", storeId);
            List<Map<String, Object>> rows = salesRecordMapper.selectMaps(qw);
            BigDecimal totalAmt = BigDecimal.ZERO;
            for (Map<String, Object> r : rows) {
                Object amt = r.get("salesAmount");
                if (amt instanceof BigDecimal) totalAmt = totalAmt.add((BigDecimal) amt);
            }
            totalAmt = totalAmt.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : totalAmt;
            int rank = 1;
            for (Map<String, Object> r : rows) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("rank", rank++);
                Long eid = r.get("employee_id") instanceof Number ? ((Number) r.get("employee_id")).longValue() : 0L;
                m.put("name", "导购#" + eid);
                m.put("code", "EMP" + eid);
                m.put("quantity", r.get("quantity") instanceof Number ? ((Number) r.get("quantity")).intValue() : 0);
                BigDecimal sa = r.get("salesAmount") instanceof BigDecimal ? (BigDecimal) r.get("salesAmount") : BigDecimal.ZERO;
                m.put("salesAmount", sa);
                m.put("percentage", sa.multiply(BigDecimal.valueOf(100)).divide(totalAmt, 1, RoundingMode.HALF_UP));
                result.add(m);
            }
        } else {
            // product: 关联 sales_item 表按 productName 聚合
            LambdaQueryWrapper<SalesRecord> recordWrapper = new LambdaQueryWrapper<SalesRecord>()
                    .ge(SalesRecord::getSalesDate, month + "-01")
                    .lt(SalesRecord::getSalesDate, nextMonth(month) + "-01");
            if (storeId != null) recordWrapper.eq(SalesRecord::getStoreId, storeId);
            List<Long> recordIds = salesRecordMapper.selectList(recordWrapper)
                    .stream().map(SalesRecord::getId).collect(Collectors.toList());
            if (!recordIds.isEmpty()) {
                List<SalesItem> items = salesItemMapper.selectList(
                        new LambdaQueryWrapper<SalesItem>().in(SalesItem::getSalesRecordId, recordIds));
                // 按 productName 聚合
                Map<String, BigDecimal> amountMap = new LinkedHashMap<>();
                Map<String, Integer> qtyMap = new LinkedHashMap<>();
                for (SalesItem item : items) {
                    String name = item.getProductName() != null ? item.getProductName() : "未知商品";
                    BigDecimal lineAmt = (item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO)
                            .multiply(BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 0));
                    amountMap.merge(name, lineAmt, BigDecimal::add);
                    qtyMap.merge(name, item.getQuantity() != null ? item.getQuantity() : 0, Integer::sum);
                }
                BigDecimal totalAmt = amountMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                totalAmt = totalAmt.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : totalAmt;
                // 按销售额降序
                List<Map.Entry<String, BigDecimal>> sorted = new ArrayList<>(amountMap.entrySet());
                sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
                int rank = 1;
                for (Map.Entry<String, BigDecimal> e : sorted) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("rank", rank++);
                    m.put("name", e.getKey());
                    m.put("code", "SKU" + (1000 + rank));
                    m.put("quantity", qtyMap.getOrDefault(e.getKey(), 0));
                    m.put("salesAmount", e.getValue());
                    m.put("percentage", e.getValue().multiply(BigDecimal.valueOf(100))
                            .divide(totalAmt, 1, RoundingMode.HALF_UP));
                    result.add(m);
                }
            }
        }
        return result;
    }

    private String nextMonth(String ym) {
        int y = Integer.parseInt(ym.substring(0, 4)), m = Integer.parseInt(ym.substring(5, 7));
        if (m == 12) return (y + 1) + "-01";
        return y + "-" + String.format("%02d", m + 1);
    }
}
