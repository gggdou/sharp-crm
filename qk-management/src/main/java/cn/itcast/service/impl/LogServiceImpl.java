package cn.itcast.service.impl;

import cn.itcast.common.PageDate;
import cn.itcast.dto.OperateLogDto;
import cn.itcast.mapper.LogMapper;
import cn.itcast.pojo.OperateLog;
import cn.itcast.service.LogService;
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
