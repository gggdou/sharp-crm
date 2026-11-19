package com.sharpcrm.mapper;

import com.sharpcrm.pojo.OperateLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志数据访问层接口
 */
@Mapper
public interface OperateLogMapper extends BaseMapper<OperateLog> {
}
