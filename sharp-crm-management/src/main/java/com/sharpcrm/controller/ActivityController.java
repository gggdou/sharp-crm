package com.sharpcrm.controller;

import com.sharpcrm.common.PageDate;
import com.sharpcrm.common.Result;
import com.sharpcrm.dto.ActivityDto;
import com.sharpcrm.pojo.Activity;
import com.sharpcrm.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动控制器，处理活动的增删改查、分页与条件查询接口
 */
@Slf4j
@RestController
@RequestMapping("/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    /**
     * 分页查询活动
     */
    @GetMapping
    public Result findPage(ActivityDto activityDto) {
        log.info("分页查询，参数：{}", activityDto);
        PageDate<Activity> pageDates = activityService.findPage(activityDto);
        return Result.success(pageDates);
    }

    /**
     * 根据id删除活动
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        log.info("根据id删除活动，参数：{}", id);
        boolean flag = activityService.removeById(id);
        return flag ? Result.success() : Result.error("删除失败");
    }

    /**
     * 新增活动
     */
    @PostMapping
    public Result addActivity(@RequestBody Activity activity) {
        log.info("添加活动，参数：{}", activity);
        // description 在库中为非空列，未填写时补空串，避免直接抛 SQL 异常
        if (activity.getDescription() == null) {
            activity.setDescription("");
        }
        boolean flag = activityService.save(activity);
        return flag ? Result.success() : Result.error("添加失败");
    }

    /**
     * 根据id查询活动详情
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        log.info("根据id查询活动，参数：{}", id);
        Activity activity = activityService.getById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        return Result.success(activity);
    }

    /**
     * 修改活动
     */
    @PutMapping
    public Result updateActivity(@RequestBody Activity activity) {
        log.info("修改活动，参数：{}", activity);
        boolean flag = activityService.updateById(activity);
        return flag ? Result.success() : Result.error("修改失败");
    }

    /**
     * 根据活动类型查询活动
     */
    @GetMapping("/type/{type}")
    public Result findByType(@PathVariable Integer type) {
        log.info("查询指定type的活动，参数：{}", type);
        List<Activity> activities = activityService.findByType(type);
        if (activities == null) {
            return Result.error("活动不存在");
        }
        return Result.success(activities);
    }

}
