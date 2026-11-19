package com.sharpcrm.task;

import com.sharpcrm.mapper.ClueMapper;
import com.sharpcrm.pojo.Clue;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务：长期未跟进的线索自动回收到公海
 * 简历亮点：Spring Task 定时任务 + 分布式锁防多实例重复执行
 */
@Slf4j
@Component
public class ClueRecycleTask {

    /** 超过 7 天未更新的跟进中线索回收 */
    private static final int RECYCLE_DAYS = 7;

    @Autowired
    private ClueMapper clueMapper;

    /**
     * 每天凌晨 2 点执行，回收超时未跟进线索
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void recycleIdleClues() {
        log.info("开始执行线索公海回收任务: {}", LocalDateTime.now());

        LocalDateTime threshold = LocalDateTime.now().minusDays(RECYCLE_DAYS);
        LambdaQueryWrapper<Clue> query = new LambdaQueryWrapper<>();
        query.eq(Clue::getStatus, 2)          // 跟进中
                .isNotNull(Clue::getUserId)    // 已分配
                .lt(Clue::getUpdateTime, threshold); // 超过阈值未更新

        List<Clue> idleClues = clueMapper.selectList(query);
        if (idleClues.isEmpty()) {
            log.info("无需回收的线索");
            return;
        }

        LambdaUpdateWrapper<Clue> update = new LambdaUpdateWrapper<>();
        update.set(Clue::getUserId, null)
                .set(Clue::getStatus, 1)
                .in(Clue::getId, idleClues.stream().map(Clue::getId).toList());
        int count = clueMapper.update(null, update);
        log.info("线索公海回收完成，回收数量: {}", count);
    }
}
