package com.sharpcrm.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.sharpcrm.common.DataHolder;
import com.sharpcrm.common.PageDate;
import com.sharpcrm.dto.ContractDto;
import com.sharpcrm.exception.CustomException;
import com.sharpcrm.mapper.ContractMapper;
import com.sharpcrm.mapper.CustomerMapper;
import com.sharpcrm.mapper.PaymentMapper;
import com.sharpcrm.mapper.UserMapper;
import com.sharpcrm.pojo.Contract;
import com.sharpcrm.pojo.Customer;
import com.sharpcrm.pojo.Payment;
import com.sharpcrm.pojo.User;
import com.sharpcrm.service.ContractService;
import com.sharpcrm.support.DataScopeHelper;
import com.sharpcrm.vo.ContractVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 合同与回款实现
 * <p>
 * 一条核心约束：回款计划的金额之和必须等于合同金额。
 * 分期用「前 n-1 期向下取整均分、最后一期承担尾差」的方式拆分，
 * 避免出现 1000 / 3 = 333.33 * 3 = 999.99 的差额。
 */
@Service
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Contract> implements ContractService {

    /** 合同状态 */
    private static final int STATUS_ACTIVE = 2;
    private static final int STATUS_DONE = 3;
    /** 回款状态 */
    private static final int PAY_WAITING = 1;
    private static final int PAY_PARTIAL = 2;
    private static final int PAY_PAID = 3;
    private static final int PAY_OVERDUE = 4;

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private DataScopeHelper dataScopeHelper;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DataHolder<?> dataHolder;

    @Override
    public PageDate<Contract> findPage(ContractDto dto) {
        Integer scope = dataScopeHelper.scopeUserId();
        LambdaQueryWrapper<Contract> query = new LambdaQueryWrapper<>();
        query.like(StrUtil.isNotBlank(dto.getContractNo()), Contract::getContractNo, dto.getContractNo())
                .eq(dto.getCustomerId() != null, Contract::getCustomerId, dto.getCustomerId())
                .eq(dto.getStatus() != null, Contract::getStatus, dto.getStatus())
                .eq(scope != null, Contract::getUserId, scope)
                .orderByDesc(Contract::getCreateTime);

        IPage<Contract> page = getBaseMapper().selectPage(new Page<>(dto.getPage(), dto.getPageSize()), query);
        List<Contract> records = page.getRecords();
        fillDerived(records);
        return new PageDate<>(page.getTotal(), records);
    }

    @Override
    public ContractVo findDetail(Integer id) {
        Contract contract = getById(id);
        if (contract == null) {
            throw new CustomException("合同不存在!");
        }
        dataScopeHelper.checkAccess(contract.getUserId(), "合同", false);

        List<Payment> payments = paymentMapper.selectList(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getContractId, id)
                .orderByAsc(Payment::getPeriod));

        BigDecimal paid = sumActual(payments);
        BigDecimal amount = contract.getAmount() == null ? BigDecimal.ZERO : contract.getAmount();
        BigDecimal unpaid = amount.subtract(paid).max(BigDecimal.ZERO);

        ContractVo vo = new ContractVo();
        vo.setContract(contract);
        vo.setCustomerName(customerName(contract.getCustomerId()));
        vo.setPayments(payments);
        vo.setPaidAmount(paid);
        vo.setUnpaidAmount(unpaid);
        vo.setProgress(percent(paid, amount));
        vo.setOverdueCount((int) payments.stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == PAY_OVERDUE)
                .count());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createContract(Contract contract, Integer periods) {
        if (contract.getCustomerId() == null) {
            throw new CustomException("请选择客户");
        }
        Customer customer = customerMapper.selectById(contract.getCustomerId());
        if (customer == null) {
            throw new CustomException("客户不存在");
        }
        if (contract.getAmount() == null || contract.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("合同金额必须大于 0");
        }

        contract.setId(null);
        contract.setContractNo(generateNo());
        contract.setStatus(contract.getStatus() == null ? STATUS_ACTIVE : contract.getStatus());
        contract.setPayType(contract.getPayType() == null ? 1 : contract.getPayType());
        contract.setSignDate(contract.getSignDate() == null ? LocalDate.now() : contract.getSignDate());
        if (contract.getUserId() == null) {
            // 签约人默认当前登录用户，管理员代签时可显式传入
            contract.setUserId(dataHolder.getCurrentUserId() != null
                    ? dataHolder.getCurrentUserId() : customer.getUserId());
        }
        save(contract);

        buildPayments(contract, periods);
        return contract.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateContract(Contract contract) {
        if (contract.getId() == null) {
            throw new CustomException("合同 id 不能为空");
        }
        Contract exist = getById(contract.getId());
        if (exist == null) {
            throw new CustomException("合同不存在!");
        }
        dataScopeHelper.checkAccess(exist.getUserId(), "合同", false);

        BigDecimal newAmount = contract.getAmount();
        if (newAmount != null && newAmount.compareTo(exist.getAmount()) != 0) {
            // 金额一旦变动，已生成的回款计划就失去意义。真实业务里这是「合同变更」，需要重新签订。
            boolean hasPaid = paymentMapper.selectCount(new LambdaQueryWrapper<Payment>()
                    .eq(Payment::getContractId, contract.getId())
                    .gt(Payment::getActualAmount, BigDecimal.ZERO)) > 0;
            if (hasPaid) {
                throw new CustomException("已有回款记录，不能修改合同金额");
            }
            exist.setAmount(newAmount);
        }
        if (StrUtil.isNotBlank(contract.getRemark())) {
            exist.setRemark(contract.getRemark());
        }
        if (contract.getSignDate() != null) {
            exist.setSignDate(contract.getSignDate());
        }
        if (contract.getCourseId() != null) {
            exist.setCourseId(contract.getCourseId());
        }
        updateById(exist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeContract(Integer id) {
        Contract contract = getById(id);
        if (contract == null) {
            return;
        }
        dataScopeHelper.checkAccess(contract.getUserId(), "合同", false);
        paymentMapper.delete(new LambdaQueryWrapper<Payment>().eq(Payment::getContractId, id));
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerPayment(Payment payment) {
        if (payment.getId() == null) {
            throw new CustomException("请选择要登记的回款期次");
        }
        Payment exist = paymentMapper.selectById(payment.getId());
        if (exist == null) {
            throw new CustomException("回款记录不存在");
        }
        Contract contract = getById(exist.getContractId());
        if (contract == null) {
            throw new CustomException("合同不存在!");
        }
        dataScopeHelper.checkAccess(contract.getUserId(), "合同", false);

        BigDecimal actual = payment.getActualAmount();
        if (actual == null || actual.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("回款金额必须大于 0");
        }

        exist.setActualAmount(actual);
        exist.setActualDate(payment.getActualDate() == null ? LocalDate.now() : payment.getActualDate());
        exist.setRemark(payment.getRemark());
        exist.setStatus(actual.compareTo(exist.getPlanAmount()) >= 0 ? PAY_PAID : PAY_PARTIAL);
        paymentMapper.updateById(exist);

        refreshContractStatus(contract.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markOverdue() {
        LambdaUpdateWrapper<Payment> update = new LambdaUpdateWrapper<>();
        update.set(Payment::getStatus, PAY_OVERDUE)
                .in(Payment::getStatus, PAY_WAITING, PAY_PARTIAL)
                .isNotNull(Payment::getPlanDate)
                .lt(Payment::getPlanDate, LocalDate.now());
        return paymentMapper.update(null, update);
    }

    /**
     * 按付款方式生成回款计划：一次性 1 期，分期默认 3 期，每期间隔一个月
     */
    private void buildPayments(Contract contract, Integer periods) {
        BigDecimal amount = contract.getAmount();
        int count = contract.getPayType() == 2
                ? Math.min(Math.max(periods == null ? 3 : periods, 2), 12)
                : 1;

        BigDecimal per = amount.divide(BigDecimal.valueOf(count), 2, RoundingMode.DOWN);
        // 尾差全部落在最后一期，保证总额严格相等
        BigDecimal last = amount.subtract(per.multiply(BigDecimal.valueOf(count - 1L)));

        LocalDate base = contract.getSignDate() == null ? LocalDate.now() : contract.getSignDate();
        List<Payment> payments = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            Payment payment = new Payment();
            payment.setContractId(contract.getId());
            payment.setPeriod(i);
            payment.setPlanAmount(i == count ? last : per);
            payment.setPlanDate(base.plusMonths(i - 1L));
            payment.setStatus(PAY_WAITING);
            payments.add(payment);
        }
        payments.forEach(paymentMapper::insert);
    }

    /**
     * 所有期次收齐后把合同置为已完成
     */
    private void refreshContractStatus(Integer contractId) {
        List<Payment> payments = paymentMapper.selectList(
                new LambdaQueryWrapper<Payment>().eq(Payment::getContractId, contractId));
        if (payments.isEmpty()) {
            return;
        }
        boolean allPaid = payments.stream()
                .allMatch(p -> p.getStatus() != null && p.getStatus() == PAY_PAID);
        if (!allPaid) {
            return;
        }
        Contract contract = getById(contractId);
        if (contract != null && contract.getStatus() != null && contract.getStatus() == STATUS_ACTIVE) {
            contract.setStatus(STATUS_DONE);
            updateById(contract);
        }
    }

    /**
     * 列表页需要客户名、归属人名、已收金额与期数，一次批量补齐，避免 N+1
     */
    private void fillDerived(List<Contract> contracts) {
        if (contracts.isEmpty()) {
            return;
        }
        Set<Integer> contractIds = contracts.stream().map(Contract::getId).collect(Collectors.toSet());
        Set<Integer> customerIds = contracts.stream()
                .map(Contract::getCustomerId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Set<Integer> userIds = contracts.stream()
                .map(Contract::getUserId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());

        Map<Integer, String> customerNames = customerIds.isEmpty() ? Collections.emptyMap()
                : customerMapper.selectBatchIds(customerIds).stream()
                .collect(Collectors.toMap(Customer::getId, c -> StrUtil.blankToDefault(c.getName(), "-")));
        Map<Integer, String> ownerNames = userIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> StrUtil.blankToDefault(u.getName(), u.getUsername())));

        Map<Integer, List<Payment>> paymentsByContract = paymentMapper.selectList(
                        new LambdaQueryWrapper<Payment>().in(Payment::getContractId, contractIds))
                .stream().collect(Collectors.groupingBy(Payment::getContractId));

        for (Contract contract : contracts) {
            contract.setCustomerName(customerNames.get(contract.getCustomerId()));
            contract.setOwnerName(ownerNames.get(contract.getUserId()));
            List<Payment> payments = paymentsByContract.getOrDefault(contract.getId(), Collections.emptyList());
            contract.setPeriodCount(payments.size());
            contract.setPaidAmount(sumActual(payments));
        }
    }

    private BigDecimal sumActual(List<Payment> payments) {
        return payments.stream()
                .map(Payment::getActualAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Integer percent(BigDecimal paid, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        int value = paid.multiply(BigDecimal.valueOf(100))
                .divide(amount, 0, RoundingMode.HALF_UP).intValue();
        return Math.min(value, 100);
    }

    private String customerName(Integer customerId) {
        if (customerId == null) {
            return null;
        }
        Customer customer = customerMapper.selectById(customerId);
        return customer == null ? null : customer.getName();
    }

    /**
     * 合同编号：HT + 日期 + 5 位随机数，唯一索引兜底，撞号则重试
     */
    private String generateNo() {
        for (int i = 0; i < 5; i++) {
            String no = "HT" + LocalDate.now().format(NO_DATE) + RandomUtil.randomNumbers(5);
            boolean exists = count(new LambdaQueryWrapper<Contract>().eq(Contract::getContractNo, no)) > 0;
            if (!exists) {
                return no;
            }
        }
        throw new CustomException("合同编号生成失败，请重试");
    }
}
