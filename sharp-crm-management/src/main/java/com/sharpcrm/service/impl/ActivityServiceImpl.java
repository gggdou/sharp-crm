package com.sharpcrm.service.impl;

import com.sharpcrm.common.PageDate;
import com.sharpcrm.dto.ActivityDto;
import com.sharpcrm.mapper.ActivityMapper;
import com.sharpcrm.pojo.Activity;
import com.sharpcrm.service.ActivityService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 活动业务层实现类，处理活动的分页与条件查询业务
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    /**
     * 分页查询活动
     */
    @Override
    public PageDate<Activity> findPage(ActivityDto activityDto) {
        IPage<Activity> page = new Page<>(activityDto.getPage(), activityDto.getPageSize());
        IPage<Activity> iPage = getBaseMapper().findPage(page, activityDto);
        return new PageDate<>(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * 根据类型查询活动
     */
    @Override
    public List<Activity> findByType(Integer type) {
        LambdaQueryWrapper<Activity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Activity::getType, type);
        return getBaseMapper().selectList(lambdaQueryWrapper);
    }

}
