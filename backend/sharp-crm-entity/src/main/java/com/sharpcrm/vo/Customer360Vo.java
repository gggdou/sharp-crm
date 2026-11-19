package com.sharpcrm.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sharpcrm.pojo.Business;
import com.sharpcrm.pojo.Clue;
import com.sharpcrm.pojo.Contract;
import com.sharpcrm.pojo.Customer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户 360 视图
 * <p>
 * 客户是销售链路的下游终点，单独一张 customer 表看不出这个客户是怎么来的。
 * 这个视图把「线索 → 商机 → 成交」全链路串起来，一次请求给到前端，
 * 避免详情页发四五个请求再自己拼时间线。
 */
@Data
public class Customer360Vo {

    /** 客户主体信息 */
    private Customer customer;

    /** 归属人姓名 */
    private String ownerName;

    /** 来源线索，可能为空（历史数据或手工录入的客户） */
    private Clue sourceClue;

    /** 来源商机，可能为空 */
    private Business sourceBusiness;

    /** 该客户名下的合同，按签订时间倒序 */
    private List<Contract> contracts;

    /** 关键指标：从线索创建到成交用了多少天、累计跟进次数 */
    private Stat stat;

    /** 时间线事件，按时间倒序 */
    private List<Event> timeline;

    @Data
    public static class Stat {
        /** 从线索首次进入到成交的天数，来源缺失时为 null */
        private Long convertDays;
        /** 线索跟进次数 */
        private Integer clueFollowCount;
        /** 商机跟进次数 */
        private Integer businessFollowCount;
        /** 累计跟进次数 */
        private Integer followCount;
        /** 合同总额 */
        private BigDecimal contractAmount;
        /** 已回款金额 */
        private BigDecimal paidAmount;
        /** 回款率 0-100 */
        private Integer receivableRate;
    }

    /**
     * 时间线条目：线索跟进、商机跟进、成交各算一条
     */
    @Data
    public static class Event {
        /** 阶段：clue / business / customer */
        private String stage;
        /** 标题 */
        private String title;
        /** 详情，可能为空 */
        private String content;
        /** 操作人姓名 */
        private String operator;
        /** 发生时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime time;
        /** 业务标签，如「接通」「拒绝」「伪线索」 */
        private String tag;
        /** 语义色：1 推进 / 0 中性 / -1 受阻 */
        private Integer tone;
    }
}
