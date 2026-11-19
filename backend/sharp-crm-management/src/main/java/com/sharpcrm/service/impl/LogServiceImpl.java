package com.sharpcrm.service.impl;

import com.sharpcrm.common.PageDate;
import com.sharpcrm.dto.OperateLogDto;
import com.sharpcrm.mapper.LogMapper;
import com.sharpcrm.pojo.OperateLog;
import com.sharpcrm.service.LogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, OperateLog> implements LogService {

    @Override
    public PageDate<OperateLog> findByPage(OperateLogDto operateLogDto) {

        IPage<OperateLog> page = new Page<>(operateLogDto.getPage(), operateLogDto.getPageSize());

        IPage<OperateLog> iPage = getBaseMapper().findByPage(page, operateLogDto);

        return new PageDate<>(iPage.getTotal(), iPage.getRecords());
    }
}
