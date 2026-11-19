package com.sharpcrm.vo;

import lombok.Data;

import java.util.List;

/**
 * 数据看板统计结果
 * <p>
 * 所有数值均由数据库实时聚合得出，不再由前端硬编码。
 */
@Data
public class DashboardVo {

    /** 顶部概览指标 */
    private Overview overview;

    /** 近 6 个月新增线索 / 转商机趋势 */
    private List<TrendPoint> trend;

    /** 线索来源分布 */
    private List<NameValue> channelDist;

    /** 线索状态分布 */
    private List<NameValue> statusDist;

    /** 转化漏斗：线索 → 商机 → 客户 */
    private List<NameValue> funnel;

    @Data
    public static class Overview {
        private long clueTotal;
        private long businessTotal;
        private long customerTotal;
        private long courseTotal;
        private long clueThisMonth;
        private long businessThisMonth;
        /** 线索转商机转化率，百分比数值，保留一位小数 */
        private double clueToBusinessRate;
        /** 商机转客户成交率，百分比数值，保留一位小数 */
        private double businessToCustomerRate;
    }

    @Data
    public static class TrendPoint {
        private String month;
        private long clueCount;
        private long businessCount;
    }

    @Data
    public static class NameValue {
        private String name;
        private long value;

        public NameValue() {
        }

        public NameValue(String name, long value) {
            this.name = name;
            this.value = value;
        }
    }
}
