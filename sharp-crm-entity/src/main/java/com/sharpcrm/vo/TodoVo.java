package com.sharpcrm.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 待办中心视图
 * <p>
 * 待办不是一张新表，而是从已有业务数据里「推导」出来的：
 * 商机设了下一次跟进时间、线索 7 天不动会被回收、合同分期有到期日。
 * 这些数据一直存在库里，只是缺少一个入口把它们聚到用户面前。
 */
@Data
public class TodoVo {

    /** 按紧急度统计，前端用于顶部三个数字 */
    private Summary summary;

    /** 明细，已按紧急度和到期时间排序 */
    private List<Item> items;

    @Data
    public static class Summary {
        /** 已逾期 */
        private long overdue;
        /** 今天到期 */
        private long today;
        /** 三天内到期 */
        private long soon;
        /** 合计 */
        private long total;
    }

    @Data
    public static class Item {
        /** 来源类型：business / clue / contract */
        private String type;
        /** 业务主键 */
        private Integer id;
        /** 客户姓名 */
        private String title;
        /** 手机号 */
        private String phone;
        /** 提示文案，如「还有 2 天未跟进将自动回收」 */
        private String detail;
        /** 到期时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime dueTime;
        /** 0 已逾期 / 1 今天 / 2 三天内 */
        private Integer urgency;
        /** 建议动作，前端按钮文案 */
        private String action;
        /** 前端跳转路径 */
        private String link;
    }
}
