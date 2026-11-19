package com.sharpcrm.service.impl;

import com.sharpcrm.mapper.OperateLogMapper;
import com.sharpcrm.pojo.OperateLog;
import com.sharpcrm.service.OperateLogService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 操作日志业务层实现类
 */
@Service
public class OperateLogServiceImpl extends ServiceImpl<OperateLogMapper, OperateLog> implements OperateLogService {
}
