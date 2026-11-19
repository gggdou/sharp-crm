package com.sharpcrm.mapper;

import com.sharpcrm.dto.ActivityDto;
import com.sharpcrm.pojo.Activity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动数据访问层接口
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    /**
     * 分页查询活动
     */
    IPage<Activity> findPage(IPage<Activity> page, ActivityDto activityDto);
}
