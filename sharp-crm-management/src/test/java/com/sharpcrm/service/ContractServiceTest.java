package com.sharpcrm.service;

import com.sharpcrm.common.DataHolder;
import com.sharpcrm.exception.CustomException;
import com.sharpcrm.mapper.ContractMapper;
import com.sharpcrm.mapper.CustomerMapper;
import com.sharpcrm.mapper.PaymentMapper;
import com.sharpcrm.mapper.UserMapper;
import com.sharpcrm.pojo.Contract;
import com.sharpcrm.pojo.Customer;
import com.sharpcrm.pojo.Payment;
import com.sharpcrm.service.impl.ContractServiceImpl;
import com.sharpcrm.support.DataScopeHelper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * 合同与回款单元测试
 * <p>
 * 重点覆盖两件最容易出错的事：分期金额必须严格等于合同总额、回款状态必须由实收金额推导。
 */
class ContractServiceTest {

    private final List<Payment> savedPayments = new ArrayList<>();

    private ContractServiceImpl newService(ContractMapper contractMapper, PaymentMapper paymentMapper) {
        ContractServiceImpl service = spy(new ContractServiceImpl());
        ReflectionTestUtils.setField(service, "baseMapper", contractMapper);

        DataHolder<?> dataHolder = mock(DataHolder.class);
        when(dataHolder.getCurrentUserId()).thenReturn(1);
        ReflectionTestUtils.setField(service, "dataHolder", dataHolder);

        ReflectionTestUtils.setField(service, "paymentMapper", paymentMapper);
        ReflectionTestUtils.setField(service, "customerMapper", mock(CustomerMapper.class));
        ReflectionTestUtils.setField(service, "userMapper", mock(UserMapper.class));
        ReflectionTestUtils.setField(service, "dataScopeHelper", mock(DataScopeHelper.class));
        return service;
    }

    private ContractMapper contractMapper() {
        ContractMapper mapper = mock(ContractMapper.class);
        when(mapper.selectCount(any())).thenReturn(0L);
        // 模拟自增主键回填
        when(mapper.insert(any(Contract.class))).thenAnswer(inv -> {
            inv.getArgument(0, Contract.class).setId(1);
            return 1;
        });
        return mapper;
    }

    private PaymentMapper paymentMapper() {
        savedPayments.clear();
        PaymentMapper mapper = mock(PaymentMapper.class);
        when(mapper.insert(any(Payment.class))).thenAnswer(inv -> {
            savedPayments.add(inv.getArgument(0));
            return 1;
        });
        return mapper;
    }

    private Customer customer() {
        Customer customer = new Customer();
        customer.setId(10);
        customer.setName("测试客户");
        customer.setUserId(1);
        return customer;
    }

    @Test
    void 分期合同的各期金额之和必须严格等于合同总额() {
        ContractMapper contractMapper = contractMapper();
        PaymentMapper paymentMapper = paymentMapper();
        ContractServiceImpl service = newService(contractMapper, paymentMapper);

        CustomerMapper customerMapper = mock(CustomerMapper.class);
        when(customerMapper.selectById(10)).thenReturn(customer());
        ReflectionTestUtils.setField(service, "customerMapper", customerMapper);

        Contract contract = new Contract();
        contract.setCustomerId(10);
        contract.setAmount(new BigDecimal("10000"));
        contract.setPayType(2);
        contract.setSignDate(LocalDate.of(2026, 11, 19));

        service.createContract(contract, 3);

        assertEquals(3, savedPayments.size());
        BigDecimal sum = savedPayments.stream()
                .map(Payment::getPlanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // 均分会产生 333.33*3=999.99 的误差，尾差必须补到最后一期
        assertEquals(0, new BigDecimal("10000").compareTo(sum));
        assertEquals(new BigDecimal("3333.33"), savedPayments.get(0).getPlanAmount());
        assertEquals(new BigDecimal("3333.34"), savedPayments.get(2).getPlanAmount());
    }

    @Test
    void 一次性付款只生成一期() {
        ContractMapper contractMapper = contractMapper();
        PaymentMapper paymentMapper = paymentMapper();
        ContractServiceImpl service = newService(contractMapper, paymentMapper);

        CustomerMapper customerMapper = mock(CustomerMapper.class);
        when(customerMapper.selectById(10)).thenReturn(customer());
        ReflectionTestUtils.setField(service, "customerMapper", customerMapper);

        Contract contract = new Contract();
        contract.setCustomerId(10);
        contract.setAmount(new BigDecimal("5000"));
        contract.setPayType(1);

        service.createContract(contract, 5);

        assertEquals(1, savedPayments.size());
        assertEquals(0, new BigDecimal("5000").compareTo(savedPayments.get(0).getPlanAmount()));
    }

    @Test
    void 分期数越界会被收敛到合法区间() {
        ContractMapper contractMapper = contractMapper();
        PaymentMapper paymentMapper = paymentMapper();
        ContractServiceImpl service = newService(contractMapper, paymentMapper);

        CustomerMapper customerMapper = mock(CustomerMapper.class);
        when(customerMapper.selectById(10)).thenReturn(customer());
        ReflectionTestUtils.setField(service, "customerMapper", customerMapper);

        Contract contract = new Contract();
        contract.setCustomerId(10);
        contract.setAmount(new BigDecimal("1200"));
        contract.setPayType(2);

        service.createContract(contract, 99);
        assertEquals(12, savedPayments.size(), "上限应收敛到 12 期");

        savedPayments.clear();
        service.createContract(contract, 0);
        assertEquals(2, savedPayments.size(), "下限应收敛到 2 期");
    }

    @Test
    void 金额为零的合同不允许签订() {
        ContractServiceImpl service = newService(contractMapper(), paymentMapper());

        CustomerMapper customerMapper = mock(CustomerMapper.class);
        when(customerMapper.selectById(10)).thenReturn(customer());
        ReflectionTestUtils.setField(service, "customerMapper", customerMapper);

        Contract contract = new Contract();
        contract.setCustomerId(10);
        contract.setAmount(BigDecimal.ZERO);
        contract.setPayType(1);

        assertThrows(CustomException.class, () -> service.createContract(contract, 1));
    }

    @Test
    void 实收少于计划金额时标记为部分回款() {
        PaymentMapper paymentMapper = mock(PaymentMapper.class);
        Payment existing = new Payment();
        existing.setId(5);
        existing.setContractId(1);
        existing.setPeriod(1);
        existing.setPlanAmount(new BigDecimal("3000"));
        existing.setStatus(1);
        when(paymentMapper.selectById(5)).thenReturn(existing);
        when(paymentMapper.updateById(any(Payment.class))).thenReturn(1);

        ContractServiceImpl service = newService(contractMapper(), paymentMapper);
        Contract contract = new Contract();
        contract.setId(1);
        contract.setUserId(1);
        contract.setAmount(new BigDecimal("3000"));
        contract.setStatus(2);
        // spy 对象的方法存根必须用 doReturn，否则会真实执行一次
        doReturn(contract).when(service).getById(1);
        doReturn(List.of(existing)).when(paymentMapper).selectList(any());

        Payment input = new Payment();
        input.setId(5);
        input.setActualAmount(new BigDecimal("1200"));
        service.registerPayment(input);

        assertEquals(2, existing.getStatus());
        assertEquals(0, new BigDecimal("1200").compareTo(existing.getActualAmount()));
    }

    @Test
    void 实收达到计划金额时标记为已回款并联动合同完成() {
        PaymentMapper paymentMapper = mock(PaymentMapper.class);
        Payment existing = new Payment();
        existing.setId(5);
        existing.setContractId(1);
        existing.setPeriod(1);
        existing.setPlanAmount(new BigDecimal("3000"));
        existing.setStatus(1);
        when(paymentMapper.selectById(5)).thenReturn(existing);
        when(paymentMapper.updateById(any(Payment.class))).thenReturn(1);
        // 唯一一期已收齐
        when(paymentMapper.selectList(any())).thenAnswer(inv -> List.of(existing));

        ContractServiceImpl service = newService(contractMapper(), paymentMapper);
        Contract contract = new Contract();
        contract.setId(1);
        contract.setUserId(1);
        contract.setAmount(new BigDecimal("3000"));
        contract.setStatus(2);
        doReturn(contract).when(service).getById(1);
        doReturn(true).when(service).updateById(any(Contract.class));
        doReturn(List.of(existing)).when(paymentMapper).selectList(any());

        Payment input = new Payment();
        input.setId(5);
        input.setActualAmount(new BigDecimal("3000"));
        service.registerPayment(input);

        assertEquals(3, existing.getStatus());
        assertEquals(3, contract.getStatus(), "全部收齐后合同应自动置为已完成");
    }
}
