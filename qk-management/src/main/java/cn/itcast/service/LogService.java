package cn.itcast.service;

import cn.itcast.common.PageDate;
import cn.itcast.dto.OperateLogDto;
import cn.itcast.pojo.OperateLog;
import com.baomidou.mybatisplus.spring.service.IService;

public interface LogService extends IService<OperateLog> {
    // 分页
    PageDate<OperateLog> findByPage(OperateLogDto operateLogDto);
}
