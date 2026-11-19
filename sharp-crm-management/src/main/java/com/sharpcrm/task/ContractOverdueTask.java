package com.sharpcrm.task;

import com.sharpcrm.service.ContractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务：回款逾期标记
 * <p>
 * 计划回款日已过但仍未收齐的期次，标记为逾期。
 * 逾期状态是「推」出来的，不依赖用户打开页面才更新，
 * 否则列表里的状态永远停留在最后一次人工操作。
 */
@Slf4j
@Component
public class ContractOverdueTask {

    @Autowired
    private ContractService contractService;

    /**
     * 每天凌晨 1 点执行，早于线索回收任务，避免两个任务争用数据库连接
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void markOverdue() {
        int count = contractService.markOverdue();
        log.info("回款逾期标记完成，本次标记 {} 期", count);
    }
}
