package com.sharpcrm.service;

import com.sharpcrm.pojo.OperateLog;

/**
 * 异步日志服务：独立Bean，保证 @Async 代理生效
 */
public interface AsyncLogService {
    void saveAsync(OperateLog operateLog);
}
