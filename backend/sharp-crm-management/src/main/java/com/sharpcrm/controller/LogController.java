package com.sharpcrm.controller;

import cn.hutool.db.Page;
import com.sharpcrm.common.PageDate;
import com.sharpcrm.common.Result;
import com.sharpcrm.common.annotation.RequireRole;
import com.sharpcrm.dto.OperateLogDto;
import com.sharpcrm.pojo.OperateLog;
import com.sharpcrm.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogController {

    // 注入LogService对象
    @Autowired
    private LogService logService;

    @RequireRole("admin")
    @GetMapping("/logs")
    public Result findByPage(OperateLogDto operateLogDto){
        PageDate<OperateLog> pageDate = logService.findByPage(operateLogDto);

        return Result.success(pageDate);
    }
}
