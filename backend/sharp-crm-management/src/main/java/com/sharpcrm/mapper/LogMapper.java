package com.sharpcrm.mapper;

import com.sharpcrm.dto.OperateLogDto;
import com.sharpcrm.pojo.OperateLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogMapper extends BaseMapper<OperateLog> {

    IPage<OperateLog> findByPage(IPage<OperateLog> page, OperateLogDto operateLogDto);
}
