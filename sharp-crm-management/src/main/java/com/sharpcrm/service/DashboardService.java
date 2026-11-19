package com.sharpcrm.service;

import com.sharpcrm.vo.DashboardVo;

/**
 * 数据看板服务
 */
public interface DashboardService {

    /**
     * 汇总看板所需全部指标，一次请求返回，避免前端多次调用
     */
    DashboardVo stats();
}
