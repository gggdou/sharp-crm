package com.sharpcrm.service.impl;

import com.sharpcrm.pojo.OperateLog;
import com.sharpcrm.service.AsyncLogService;
import com.sharpcrm.service.OperateLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 异步日志服务实现：用独立线程池写库，日志失败不影响主业务
 */
@Service
public class AsyncLogServiceImpl implements AsyncLogService {

    @Autowired
    private OperateLogService operateLogService;

    @Override
    @Async("asyncExecutor")
    public void saveAsync(OperateLog operateLog) {
        try {
            operateLogService.saveOrUpdate(operateLog);
        } catch (Exception e) {
            // 日志写入失败静默处理，不阻塞主业务
            System.err.println("操作日志写入失败: " + e.getMessage());
        }
    }
}
