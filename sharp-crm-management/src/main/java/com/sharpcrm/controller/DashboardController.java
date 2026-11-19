package com.sharpcrm.controller;

import com.sharpcrm.common.Result;
import com.sharpcrm.service.DashboardService;
import com.sharpcrm.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据看板接口
 */
@Tag(name = "数据看板", description = "首页统计指标、趋势、分布与待办")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private TodoService todoService;

    @Operation(summary = "看板统计数据", description = "一次返回概览指标、近6个月趋势、来源与状态分布、转化漏斗")
    @GetMapping("/stats")
    public Result stats() {
        return Result.success(dashboardService.stats());
    }

    @Operation(summary = "我的待办", description = "聚合商机跟进到期、线索回收预警、合同回款到期，按紧急度排序")
    @GetMapping("/todos")
    public Result todos() {
        return Result.success(todoService.list());
    }
}
