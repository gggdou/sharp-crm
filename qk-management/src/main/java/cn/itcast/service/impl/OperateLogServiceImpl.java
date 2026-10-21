package cn.itcast.service.impl;

import cn.itcast.mapper.OperateLogMapper;
import cn.itcast.pojo.OperateLog;
import cn.itcast.service.OperateLogService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 操作日志业务层实现类
 */
@Service
public class OperateLogServiceImpl extends ServiceImpl<OperateLogMapper, OperateLog> implements OperateLogService {
}
