package com.sharpcrm.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.sharpcrm.common.PageDate;
import com.sharpcrm.dto.CustomerDto;
import com.sharpcrm.exception.CustomException;
import com.sharpcrm.mapper.BusinessMapper;
import com.sharpcrm.mapper.ClueMapper;
import com.sharpcrm.mapper.ContractMapper;
import com.sharpcrm.mapper.CustomerMapper;
import com.sharpcrm.mapper.PaymentMapper;
import com.sharpcrm.mapper.UserMapper;
import com.sharpcrm.pojo.Business;
import com.sharpcrm.pojo.BusinessTrackRecord;
import com.sharpcrm.pojo.Clue;
import com.sharpcrm.pojo.ClueTrackRecord;
import com.sharpcrm.pojo.Contract;
import com.sharpcrm.pojo.Customer;
import com.sharpcrm.pojo.Payment;
import com.sharpcrm.pojo.User;
import com.sharpcrm.service.BusinessTrackRecordService;
import com.sharpcrm.service.ClueTrackRecordService;
import com.sharpcrm.service.CustomerService;
import com.sharpcrm.support.DataScopeHelper;
import com.sharpcrm.vo.Customer360Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 客户业务实现
 */
@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    /** 渠道来源：1 线上活动，2 推广介绍 */
    private static final Map<Integer, String> CHANNEL_NAMES = Map.of(1, "线上活动", 2, "推广介绍");

    /** 意向学科字典，与线索、商机共用同一套编码 */
    private static final Map<Integer, String> SUBJECT_NAMES = Map.of(
            1, "AI 应用开发", 2, "AI 大模型开发", 3, "AI 鸿蒙开发", 4, "AI 大数据",
            5, "AI 嵌入式", 6, "AI 测试", 7, "AI 运维");

    /** 商机跟进结果：1 接通，2 拒绝，3 无人接听 */
    private static final Map<Integer, String> TRACK_STATUS_NAMES = Map.of(1, "接通", 2, "客户拒绝", 3, "无人接听");

    @Autowired
    private DataScopeHelper dataScopeHelper;

    @Autowired
    private ClueMapper clueMapper;

    @Autowired
    private BusinessMapper businessMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ContractMapper contractMapper;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private ClueTrackRecordService clueTrackRecordService;

    @Autowired
    private BusinessTrackRecordService businessTrackRecordService;

    @Override
    public PageDate<Customer> findPage(CustomerDto dto) {
        IPage<Customer> page = new Page<>(dto.getPage(), dto.getPageSize());
        // 数据权限：管理员不限，其余角色强制只看自己名下客户
        Integer scope = dataScopeHelper.scopeUserId();
        LambdaQueryWrapper<Customer> query = new LambdaQueryWrapper<>();
        query.like(StrUtil.isNotBlank(dto.getName()), Customer::getName, dto.getName())
                .like(StrUtil.isNotBlank(dto.getPhone()), Customer::getPhone, dto.getPhone())
                .eq(dto.getSubject() != null, Customer::getSubject, dto.getSubject());
        if (scope != null) {
            query.eq(Customer::getUserId, scope);
        } else {
            // 仅管理员可按归属人筛选，普通角色的范围条件已固定为本人
            query.eq(dto.getUserId() != null, Customer::getUserId, dto.getUserId());
        }
        IPage<Customer> result = getBaseMapper().selectPage(page, query);
        return new PageDate<>(result.getTotal(), result.getRecords());
    }

    @Override
    public java.util.List<Customer> listWithScope() {
        Integer scope = dataScopeHelper.scopeUserId();
        if (scope == null) {
            return list();
        }
        return list(new LambdaQueryWrapper<Customer>().eq(Customer::getUserId, scope));
    }

    @Override
    public Customer findByIdWithScope(Integer id) {
        Customer customer = getById(id);
        if (customer != null) {
            // 客户是成交后的正式数据，不存在公海，归属为空时同样按无权处理
            dataScopeHelper.checkAccess(customer.getUserId(), "客户", false);
        }
        return customer;
    }

    /**
     * 客户 360：一次查出客户主体、来源线索、来源商机，再把两边的跟进记录合并成一条时间线
     */
    @Override
    public Customer360Vo find360(Integer id) {
        Customer customer = getById(id);
        if (customer == null) {
            throw new CustomException("客户不存在!");
        }
        dataScopeHelper.checkAccess(customer.getUserId(), "客户", false);

        Clue clue = customer.getClueId() == null ? null : clueMapper.selectById(customer.getClueId());
        Business business = customer.getBusinessId() == null ? null : businessMapper.selectById(customer.getBusinessId());

        // 旧库迁移遗留：部分客户的 clue_id / business_id 指向了别人的记录。
        // 手机号是客户唯一身份标识，对不上说明关联已失效，宁可显示「无溯源」也不展示错误的链路。
        if (!belongsTo(clue == null ? null : clue.getPhone(), customer.getPhone())) {
            clue = null;
        }
        if (!belongsTo(business == null ? null : business.getPhone(), customer.getPhone())) {
            business = null;
        }

        List<ClueTrackRecord> clueRecords = clue == null ? Collections.emptyList()
                : clueTrackRecordService.list(new LambdaQueryWrapper<ClueTrackRecord>()
                        .eq(ClueTrackRecord::getClueId, clue.getId())
                        .orderByDesc(ClueTrackRecord::getCreateTime));

        List<BusinessTrackRecord> businessRecords = business == null ? Collections.emptyList()
                : businessTrackRecordService.list(new LambdaQueryWrapper<BusinessTrackRecord>()
                        .eq(BusinessTrackRecord::getBusinessId, business.getId())
                        .orderByDesc(BusinessTrackRecord::getCreateTime));

        // 合同与回款：客户名下的签约与收款记录
        List<Contract> contracts = contractMapper.selectList(new LambdaQueryWrapper<Contract>()
                .eq(Contract::getCustomerId, customer.getId())
                .orderByDesc(Contract::getSignDate));
        List<Payment> payments = contracts.isEmpty() ? Collections.emptyList()
                : paymentMapper.selectList(new LambdaQueryWrapper<Payment>()
                        .in(Payment::getContractId, contracts.stream().map(Contract::getId).toList())
                        .orderByAsc(Payment::getPeriod));

        Map<Integer, String> operatorNames = loadOperatorNames(customer, clue, business, contracts,
                clueRecords, businessRecords);

        List<Customer360Vo.Event> events = new ArrayList<>();
        if (clue != null) {
            events.add(event("clue", "线索进入池",
                    "渠道：" + nameOf(CHANNEL_NAMES, clue.getChannel())
                            + "，意向学科：" + nameOf(SUBJECT_NAMES, clue.getSubject()),
                    nameOf(operatorNames, clue.getUserId()), clue.getCreateTime(), null, 0));
            for (ClueTrackRecord record : clueRecords) {
                boolean falseClue = record.getType() != null && record.getType() == 0;
                events.add(event("clue",
                        falseClue ? "标记为伪线索" : "线索跟进",
                        record.getRecord(),
                        nameOf(operatorNames, record.getUserId()),
                        record.getCreateTime(),
                        falseClue ? "伪线索" : null,
                        falseClue ? -1 : 1));
            }
        }
        if (business != null) {
            events.add(event("business", "转为商机",
                    "意向学科：" + nameOf(SUBJECT_NAMES, business.getSubject()),
                    nameOf(operatorNames, business.getUserId()), business.getCreateTime(), null, 1));
            for (BusinessTrackRecord record : businessRecords) {
                Integer status = record.getTrackStatus();
                events.add(event("business", "商机跟进",
                        record.getRecord(),
                        nameOf(operatorNames, record.getUserId()),
                        record.getCreateTime(),
                        nameOf(TRACK_STATUS_NAMES, status),
                        status != null && status == 2 ? -1 : 1));
            }
        }
        events.add(event("customer", "成交，转为正式客户",
                StrUtil.blankToDefault(customer.getRemark(), "归属 " + StrUtil.blankToDefault(nameOf(operatorNames, customer.getUserId()), "未分配")),
                nameOf(operatorNames, customer.getUserId()), customer.getCreateTime(), "已成交", 1));

        // 签约与回款事件：钱的部分才是成交闭环的终点
        Map<Integer, Contract> contractMap = contracts.stream()
                .collect(Collectors.toMap(Contract::getId, c -> c, (a, b) -> a));
        for (Contract contract : contracts) {
            String detail = (contract.getPayType() != null && contract.getPayType() == 2 ? "分期付款" : "一次性付清")
                    + "，合同金额 " + contract.getAmount() + " 元";
            events.add(event("contract", "签订合同 " + contract.getContractNo(), detail,
                    nameOf(operatorNames, contract.getUserId()),
                    contract.getSignDate() == null
                            ? contract.getCreateTime()
                            : contract.getSignDate().atStartOfDay(),
                    "已签约", 1));
        }
        for (Payment payment : payments) {
            if (payment.getActualAmount() == null) {
                continue;
            }
            Contract contract = contractMap.get(payment.getContractId());
            String no = contract == null ? "" : "，合同 " + contract.getContractNo();
            events.add(event("contract", "第 " + payment.getPeriod() + " 期回款",
                    "实收 " + payment.getActualAmount() + " 元" + no,
                    null,
                    payment.getActualDate() == null ? null : payment.getActualDate().atStartOfDay(),
                    payment.getStatus() != null && payment.getStatus() == 3 ? "已收齐" : "部分回款", 1));
        }

        // 按时间倒序；历史数据缺时间的排到最后，不影响主流程展示
        events.sort((a, b) -> {
            if (a.getTime() == null && b.getTime() == null) {
                return 0;
            }
            if (a.getTime() == null) {
                return 1;
            }
            if (b.getTime() == null) {
                return -1;
            }
            return b.getTime().compareTo(a.getTime());
        });

        BigDecimal contractAmount = contracts.stream().map(Contract::getAmount)
                .filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paidAmount = payments.stream().map(Payment::getActualAmount)
                .filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        Customer360Vo.Stat stat = buildStat(customer, clue, business, clueRecords.size(), businessRecords.size());
        stat.setContractAmount(contractAmount);
        stat.setPaidAmount(paidAmount);
        stat.setReceivableRate(percent(paidAmount, contractAmount));

        Customer360Vo vo = new Customer360Vo();
        vo.setCustomer(customer);
        vo.setOwnerName(nameOf(operatorNames, customer.getUserId()));
        vo.setSourceClue(clue);
        vo.setSourceBusiness(business);
        vo.setContracts(contracts);
        vo.setTimeline(events);
        vo.setStat(stat);
        return vo;
    }

    /**
     * 回款率，合同额为 0 时返回 0，不会出现除零
     */
    private Integer percent(BigDecimal paid, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        int value = paid.multiply(BigDecimal.valueOf(100))
                .divide(amount, 0, java.math.RoundingMode.HALF_UP).intValue();
        return Math.min(value, 100);
    }

    private Customer360Vo.Stat buildStat(Customer customer, Clue clue, Business business,
                                        int clueFollowCount, int businessFollowCount) {
        Customer360Vo.Stat stat = new Customer360Vo.Stat();
        stat.setClueFollowCount(clueFollowCount);
        stat.setBusinessFollowCount(businessFollowCount);
        stat.setFollowCount(clueFollowCount + businessFollowCount);

        // 转化周期从最早可追溯的起点算起：优先线索，其次商机
        LocalDateTime start = clue != null ? clue.getCreateTime()
                : business != null ? business.getCreateTime() : null;
        if (start != null && customer.getCreateTime() != null) {
            long days = ChronoUnit.DAYS.between(start, customer.getCreateTime());
            stat.setConvertDays(Math.max(days, 0));
        }
        return stat;
    }

    /**
     * 批量取时间线上涉及的所有人姓名，避免逐条查库
     */
    private Map<Integer, String> loadOperatorNames(Customer customer, Clue clue, Business business,
                                                   List<Contract> contracts,
                                                   List<ClueTrackRecord> clueRecords,
                                                   List<BusinessTrackRecord> businessRecords) {
        Set<Integer> ids = new HashSet<>();
        addIfPresent(ids, customer.getUserId());
        if (clue != null) {
            addIfPresent(ids, clue.getUserId());
        }
        if (business != null) {
            addIfPresent(ids, business.getUserId());
        }
        contracts.forEach(c -> addIfPresent(ids, c.getUserId()));
        clueRecords.forEach(r -> addIfPresent(ids, r.getUserId()));
        businessRecords.forEach(r -> addIfPresent(ids, r.getUserId()));

        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<User> users = userMapper.selectBatchIds(ids);
        Map<Integer, String> names = new HashMap<>(users.size());
        for (User user : users) {
            names.put(user.getId(), StrUtil.blankToDefault(user.getName(), user.getUsername()));
        }
        return names;
    }

    private void addIfPresent(Set<Integer> ids, Integer id) {
        if (id != null) {
            ids.add(id);
        }
    }

    /**
     * 校验上游记录的手机号与客户是否一致，用于识别失效的历史关联
     */
    private boolean belongsTo(String upstreamPhone, String customerPhone) {
        return upstreamPhone != null && upstreamPhone.equals(customerPhone);
    }

    private <K> String nameOf(Map<K, String> dict, K key) {
        return key == null ? null : dict.get(key);
    }

    private Customer360Vo.Event event(String stage, String title, String content, String operator,
                                      LocalDateTime time, String tag, Integer tone) {
        Customer360Vo.Event event = new Customer360Vo.Event();
        event.setStage(stage);
        event.setTitle(title);
        event.setContent(content);
        event.setOperator(operator);
        event.setTime(time);
        event.setTag(tag);
        event.setTone(tone);
        return event;
    }
}
