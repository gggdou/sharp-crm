package com.sharpcrm.service;

import com.sharpcrm.common.PageDate;
import com.sharpcrm.dto.ActivityDto;
import com.sharpcrm.pojo.Activity;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

/**
 * 活动业务层接口，定义活动相关的业务方法
 */
public interface ActivityService extends IService<Activity> {

    /**
     * 分页查询活动
     */
    PageDate<Activity> findPage(ActivityDto activityDto);

    /**
     * 根据类型查询活动
     */
    List<Activity> findByType(Integer type);
}
