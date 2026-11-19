package com.sharpcrm.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.sharpcrm.common.PageDate;
import com.sharpcrm.dto.ContractDto;
import com.sharpcrm.pojo.Contract;
import com.sharpcrm.pojo.Payment;
import com.sharpcrm.vo.ContractVo;

/**
 * 合同与回款服务
 */
public interface ContractService extends IService<Contract> {

    /** 合同分页，按数据权限过滤 */
    PageDate<Contract> findPage(ContractDto dto);

    /** 合同详情：含每期回款与回款进度 */
    ContractVo findDetail(Integer id);

    /** 签订合同，同时按付款方式生成回款计划，返回新合同 id */
    Integer createContract(Contract contract, Integer periods);

    /** 修改合同：已有回款记录时不允许修改金额 */
    void updateContract(Contract contract);

    /** 删除合同，级联删除回款计划 */
    void removeContract(Integer id);

    /** 回款登记：写入实收金额与日期，并联动合同与逾期状态 */
    void registerPayment(Payment payment);

    /** 标记逾期回款，供定时任务调用，返回受影响期数 */
    int markOverdue();
}
