package com.sharpcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sharpcrm.mapper.BusinessMapper;
import com.sharpcrm.mapper.ClueMapper;
import com.sharpcrm.mapper.ContractMapper;
import com.sharpcrm.mapper.CustomerMapper;
import com.sharpcrm.mapper.PaymentMapper;
import com.sharpcrm.pojo.Business;
import com.sharpcrm.pojo.Clue;
import com.sharpcrm.pojo.Contract;
import com.sharpcrm.pojo.Customer;
import com.sharpcrm.pojo.Payment;
import com.sharpcrm.service.TodoService;
import com.sharpcrm.support.DataScopeHelper;
import com.sharpcrm.vo.TodoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 待办中心实现
 * <p>
 * 只读聚合，不写任何表：待办是「算出来的」，不是「存出来的」。
 * 好处是永远不会出现待办表与实际业务状态不一致的问题。
 */
@Service
public class TodoServiceImpl implements TodoService {

    /** 线索超过该天数未跟进会被定时任务回收，与 ClueRecycleTask 保持一致 */
    private static final int RECYCLE_DAYS = 7;

    /** 提前多少天开始提醒 */
    private static final int SOON_DAYS = 3;

    private static final DateTimeFormatter HOUR_MINUTE = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private BusinessMapper businessMapper;

    @Autowired
    private ClueMapper clueMapper;

    @Autowired
    private DataScopeHelper dataScopeHelper;

    @Autowired
    private ContractMapper contractMapper;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Override
    public TodoVo list() {
        LocalDateTime now = LocalDateTime.now();
        Integer scope = dataScopeHelper.scopeUserId();

        List<TodoVo.Item> items = new ArrayList<>();
        items.addAll(businessTodos(now, scope));
        items.addAll(clueTodos(now, scope));
        items.addAll(contractTodos(now, scope));

        // 紧急度优先，同紧急度按到期时间升序
        items.sort(Comparator.comparing(TodoVo.Item::getUrgency)
                .thenComparing(TodoVo.Item::getDueTime, Comparator.nullsLast(Comparator.naturalOrder())));

        TodoVo vo = new TodoVo();
        vo.setItems(items);
        vo.setSummary(summarize(items));
        return vo;
    }

    /**
     * 商机待跟进：状态为待跟进/跟进中，且下一次跟进时间在三天内或已逾期
     */
    private List<TodoVo.Item> businessTodos(LocalDateTime now, Integer scope) {
        LambdaQueryWrapper<Business> query = new LambdaQueryWrapper<>();
        query.in(Business::getStatus, 2, 3)
                .isNotNull(Business::getNextTime)
                .le(Business::getNextTime, now.plusDays(SOON_DAYS))
                .eq(scope != null, Business::getUserId, scope)
                .orderByAsc(Business::getNextTime);

        List<TodoVo.Item> items = new ArrayList<>();
        for (Business business : businessMapper.selectList(query)) {
            LocalDateTime due = business.getNextTime();
            int urgency = urgencyOf(due, now);
            String detail;
            if (urgency == 0) {
                long overdueDays = Math.max(ChronoUnit.DAYS.between(due, now), 1);
                detail = "已逾期 " + overdueDays + " 天，尽快联系";
            } else if (urgency == 1) {
                detail = "今天 " + due.format(HOUR_MINUTE) + " 该跟进";
            } else {
                detail = ChronoUnit.DAYS.between(now.toLocalDate(), due.toLocalDate()) + " 天后该跟进";
            }
            items.add(item("business", business.getId(), business.getName(), business.getPhone(),
                    detail, due, urgency, "去跟进", "/business"));
        }
        return items;
    }

    /**
     * 线索回收预警：跟进中的线索接近 7 天未更新，再不处理就会被定时任务收回公海
     */
    private List<TodoVo.Item> clueTodos(LocalDateTime now, Integer scope) {
        LocalDateTime warnBefore = now.minusDays(RECYCLE_DAYS - SOON_DAYS);
        LambdaQueryWrapper<Clue> query = new LambdaQueryWrapper<>();
        query.eq(Clue::getStatus, 2)
                .isNotNull(Clue::getUserId)
                .isNotNull(Clue::getUpdateTime)
                .lt(Clue::getUpdateTime, warnBefore)
                .eq(scope != null, Clue::getUserId, scope)
                .orderByAsc(Clue::getUpdateTime);

        List<TodoVo.Item> items = new ArrayList<>();
        for (Clue clue : clueMapper.selectList(query)) {
            LocalDateTime recycleAt = clue.getUpdateTime().plusDays(RECYCLE_DAYS);
            long remainingDays = ChronoUnit.DAYS.between(now, recycleAt);
            String detail;
            int urgency;
            if (remainingDays < 0) {
                urgency = 0;
                detail = "已超期未跟进，等待回收";
            } else if (remainingDays == 0) {
                urgency = 1;
                detail = "今天内不跟进将自动回收";
            } else {
                urgency = 2;
                detail = "还有 " + remainingDays + " 天未跟进将自动回收";
            }
            items.add(item("clue", clue.getId(), clue.getName(), clue.getPhone(),
                    detail, recycleAt, urgency, "去跟进", "/clue"));
        }
        return items;
    }

    /**
     * 回款到期：生效中的合同里有未收齐的期次，计划日期在三天内或已逾期
     */
    private List<TodoVo.Item> contractTodos(LocalDateTime now, Integer scope) {
        List<Contract> contracts = contractMapper.selectList(new LambdaQueryWrapper<Contract>()
                .eq(scope != null, Contract::getUserId, scope)
                .eq(Contract::getStatus, 2));
        if (contracts.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Integer, Contract> contractMap = contracts.stream()
                .collect(Collectors.toMap(Contract::getId, c -> c, (a, b) -> a));

        LocalDate today = LocalDate.now();
        List<Payment> payments = paymentMapper.selectList(new LambdaQueryWrapper<Payment>()
                .in(Payment::getContractId, contractMap.keySet())
                .in(Payment::getStatus, 1, 2, 4)
                .isNotNull(Payment::getPlanDate)
                .le(Payment::getPlanDate, today.plusDays(SOON_DAYS))
                .orderByAsc(Payment::getPlanDate));
        if (payments.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, String> names = customerNames(contracts);
        List<TodoVo.Item> items = new ArrayList<>();
        for (Payment payment : payments) {
            Contract contract = contractMap.get(payment.getContractId());
            LocalDate planDate = payment.getPlanDate();
            // 按自然日判断，避免「今天到期」被时刻差算成逾期
            int urgency = planDate.isBefore(today) ? 0 : planDate.isEqual(today) ? 1 : 2;
            String detail;
            if (urgency == 0) {
                long overdueDays = Math.max(ChronoUnit.DAYS.between(planDate, today), 1);
                detail = "第 " + payment.getPeriod() + " 期回款 " + payment.getPlanAmount() + " 元已逾期 " + overdueDays + " 天";
            } else if (urgency == 1) {
                detail = "第 " + payment.getPeriod() + " 期回款 " + payment.getPlanAmount() + " 元今天到期";
            } else {
                detail = "第 " + payment.getPeriod() + " 期回款 " + payment.getPlanAmount()
                        + " 元 " + ChronoUnit.DAYS.between(today, planDate) + " 天后到期";
            }
            items.add(item("contract", payment.getId(), names.get(contract.getCustomerId()), null,
                    detail, planDate.atStartOfDay(), urgency, "去登记", "/contract"));
        }
        return items;
    }

    private Map<Integer, String> customerNames(List<Contract> contracts) {
        List<Integer> ids = contracts.stream().map(Contract::getCustomerId)
                .filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return customerMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Customer::getId, c -> c.getName() == null ? "-" : c.getName()));
    }

    private int urgencyOf(LocalDateTime due, LocalDateTime now) {
        if (due.isBefore(now)) {
            return 0;
        }
        if (due.toLocalDate().equals(now.toLocalDate())) {
            return 1;
        }
        return 2;
    }

    private TodoVo.Item item(String type, Integer id, String title, String phone, String detail,
                             LocalDateTime dueTime, Integer urgency, String action, String link) {
        TodoVo.Item item = new TodoVo.Item();
        item.setType(type);
        item.setId(id);
        item.setTitle(title);
        item.setPhone(phone);
        item.setDetail(detail);
        item.setDueTime(dueTime);
        item.setUrgency(urgency);
        item.setAction(action);
        item.setLink(link);
        return item;
    }

    private TodoVo.Summary summarize(List<TodoVo.Item> items) {
        TodoVo.Summary summary = new TodoVo.Summary();
        summary.setTotal(items.size());
        summary.setOverdue(items.stream().filter(i -> i.getUrgency() == 0).count());
        summary.setToday(items.stream().filter(i -> i.getUrgency() == 1).count());
        summary.setSoon(items.stream().filter(i -> i.getUrgency() == 2).count());
        return summary;
    }
}
