package cn.itcast.controller;

import cn.hutool.db.Page;
import cn.itcast.common.PageDate;
import cn.itcast.common.Result;
import cn.itcast.dto.OperateLogDto;
import cn.itcast.pojo.OperateLog;
import cn.itcast.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogController {

    // 注入LogService对象
    @Autowired
    private LogService logService;

    @GetMapping("/logs")
    public Result findByPage(OperateLogDto operateLogDto){
        PageDate<OperateLog> pageDate = logService.findByPage(operateLogDto);

        return Result.success(pageDate);
    }
}
