package com.sharpcrm.service;

import com.sharpcrm.common.PageDate;
import com.sharpcrm.dto.OperateLogDto;
import com.sharpcrm.pojo.OperateLog;
import com.baomidou.mybatisplus.spring.service.IService;

public interface LogService extends IService<OperateLog> {
    // 分页
    PageDate<OperateLog> findByPage(OperateLogDto operateLogDto);
}
