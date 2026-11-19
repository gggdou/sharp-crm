package com.sharpcrm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sharpcrm.mapper.BusinessMapper;
import com.sharpcrm.mapper.ClueMapper;
import com.sharpcrm.mapper.CourseMapper;
import com.sharpcrm.mapper.CustomerMapper;
import com.sharpcrm.mapper.DashboardMapper;
import com.sharpcrm.pojo.Business;
import com.sharpcrm.pojo.Clue;
import com.sharpcrm.pojo.Customer;
import com.sharpcrm.service.DashboardService;
import com.sharpcrm.support.DataScopeHelper;
import com.sharpcrm.vo.DashboardVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据看板实现：从数据库实时聚合，替代原先写死在前端的示例数据
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    /** 趋势图展示的月份数（含本月） */
    private static final int TREND_MONTHS = 6;

    /** 渠道字典，与 clue.channel 字段注释保持一致 */
    private static final Map<Integer, String> CHANNEL_NAMES = Map.of(
            1, "线上活动",
            2, "推广介绍");

    /** 线索状态字典，与 ClueServiceImpl 中的状态语义保持一致 */
    private static final Map<Integer, String> STATUS_NAMES = Map.of(
            1, "待分配",
            2, "跟进中",
            3, "伪线索",
            4, "已转商机");

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private ClueMapper clueMapper;

    @Autowired
    private BusinessMapper businessMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private DataScopeHelper dataScopeHelper;

    @Override
    public DashboardVo stats() {
        DashboardVo vo = new DashboardVo();

        // 各业务总量：与列表接口保持一致的数据权限，管理员统计全量，其余角色只统计本人名下
        Integer scope = dataScopeHelper.scopeUserId();
        long clueTotal = clueMapper.selectCount(scope == null ? null
                : Wrappers.<Clue>lambdaQuery().eq(Clue::getUserId, scope));
        long businessTotal = businessMapper.selectCount(scope == null ? null
                : Wrappers.<Business>lambdaQuery().eq(Business::getUserId, scope));
        long customerTotal = customerMapper.selectCount(scope == null ? null
                : Wrappers.<Customer>lambdaQuery().eq(Customer::getUserId, scope));
        // 课程属于基础字典数据，所有角色看到的总量一致
        long courseTotal = courseMapper.selectCount(null);

        // 双线趋势，按月份补齐，避免某月无数据时折线断裂
        Map<String, Long> clueByMonth = toCountMap(dashboardMapper.countClueByMonth(TREND_MONTHS - 1, scope));
        Map<String, Long> businessByMonth = toCountMap(dashboardMapper.countBusinessByMonth(TREND_MONTHS - 1, scope));

        List<DashboardVo.TrendPoint> trend = new ArrayList<>(TREND_MONTHS);
        YearMonth cursor = YearMonth.now().minusMonths(TREND_MONTHS - 1L);
        for (int i = 0; i < TREND_MONTHS; i++) {
            String key = cursor.plusMonths(i).toString();
            DashboardVo.TrendPoint point = new DashboardVo.TrendPoint();
            point.setMonth(key);
            point.setClueCount(clueByMonth.getOrDefault(key, 0L));
            point.setBusinessCount(businessByMonth.getOrDefault(key, 0L));
            trend.add(point);
        }
        vo.setTrend(trend);

        // 转化率按状态统计真实转化数，而不是用下游总量除以上游总量
        // 后者在线索被回收、商机来自历史数据时会出现大于 100% 的荒谬结果
        long clueConverted = clueMapper.selectCount(Wrappers.<Clue>lambdaQuery()
                .eq(Clue::getStatus, 4)
                .eq(scope != null, Clue::getUserId, scope));
        long businessConverted = businessMapper.selectCount(Wrappers.<Business>lambdaQuery()
                .eq(Business::getStatus, 5)
                .eq(scope != null, Business::getUserId, scope));

        // 顶部概览
        DashboardVo.Overview overview = new DashboardVo.Overview();
        overview.setClueTotal(clueTotal);
        overview.setBusinessTotal(businessTotal);
        overview.setCustomerTotal(customerTotal);
        overview.setCourseTotal(courseTotal);
        // 趋势数组末位即本月
        DashboardVo.TrendPoint current = trend.get(trend.size() - 1);
        overview.setClueThisMonth(current.getClueCount());
        overview.setBusinessThisMonth(current.getBusinessCount());
        overview.setClueToBusinessRate(percent(clueConverted, clueTotal));
        overview.setBusinessToCustomerRate(percent(businessConverted, businessTotal));
        vo.setOverview(overview);

        vo.setChannelDist(translate(dashboardMapper.countClueByChannel(scope), CHANNEL_NAMES, "未知来源"));
        vo.setStatusDist(translate(dashboardMapper.countClueByStatus(scope), STATUS_NAMES, "其他状态"));

        List<DashboardVo.NameValue> funnel = new ArrayList<>(3);
        funnel.add(new DashboardVo.NameValue("线索", clueTotal));
        funnel.add(new DashboardVo.NameValue("商机", businessTotal));
        funnel.add(new DashboardVo.NameValue("客户", customerTotal));
        vo.setFunnel(funnel);

        return vo;
    }

    /**
     * 把 label/cnt 两列的结果集转成 月份 -> 数量 的映射
     */
    private Map<String, Long> toCountMap(List<Map<String, Object>> rows) {
        Map<String, Long> result = new HashMap<>();
        if (rows == null) {
            return result;
        }
        for (Map<String, Object> row : rows) {
            Object label = readIgnoreCase(row, "label");
            Object cnt = readIgnoreCase(row, "cnt");
            if (label != null && cnt != null) {
                result.put(label.toString(), Long.parseLong(cnt.toString()));
            }
        }
        return result;
    }

    /**
     * 按字典把编码翻译成中文名称，字典缺失时保留原始编码便于排查
     */
    private List<DashboardVo.NameValue> translate(List<Map<String, Object>> rows,
                                                  Map<Integer, String> dict,
                                                  String fallback) {
        List<DashboardVo.NameValue> list = new ArrayList<>();
        if (rows == null) {
            return list;
        }
        for (Map<String, Object> row : rows) {
            Object label = readIgnoreCase(row, "label");
            Object cnt = readIgnoreCase(row, "cnt");
            if (label == null || cnt == null) {
                continue;
            }
            String name = fallback;
            try {
                name = dict.getOrDefault(Integer.parseInt(label.toString()), fallback);
            } catch (NumberFormatException ignored) {
                // 编码异常时沿用兜底名称
            }
            list.add(new DashboardVo.NameValue(name, Long.parseLong(cnt.toString())));
        }
        return list;
    }

    /**
     * 列名大小写不同数据库驱动返回可能不一致，这里忽略大小写取值
     */
    private Object readIgnoreCase(Map<String, Object> row, String key) {
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 计算百分比，分母为 0 时返回 0，保留一位小数
     */
    private double percent(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(numerator * 100.0 / denominator)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
