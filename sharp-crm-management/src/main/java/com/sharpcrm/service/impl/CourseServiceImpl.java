package com.sharpcrm.service.impl;

import cn.hutool.core.util.StrUtil;
import com.sharpcrm.common.PageDate;
import com.sharpcrm.common.RedisCache;
import com.sharpcrm.dto.CourseDto;
import com.sharpcrm.mapper.CourseMapper;
import com.sharpcrm.pojo.Course;
import com.sharpcrm.service.CourseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程业务层实现类，处理课程相关的增删改查业务
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private RedisCache redisCache;

    /** 课程列表缓存 key */
    private static final String CACHE_COURSE_ALL = "course:all";

    /**
     * 分页+条件查询课程
     */
    @Override
    public PageDate<Course> findPage(CourseDto courseDto) {
        IPage<Course> page = new Page<>(courseDto.getPage(), courseDto.getPageSize());
        LambdaQueryWrapper<Course> lqw = new LambdaQueryWrapper<>();
        lqw.like(StrUtil.isNotBlank(courseDto.getName()), Course::getName, courseDto.getName());
        lqw.eq(courseDto.getSubject() != null, Course::getSubject, courseDto.getSubject());
        lqw.eq(courseDto.getTarget() != null, Course::getTarget, courseDto.getTarget());
        courseMapper.selectPage(page, lqw);
        return new PageDate<>(page.getTotal(), page.getRecords());
    }

    /**
     * 根据id删除课程
     */
    @Override
    public boolean deleteById(Integer id) {
        boolean ok = courseMapper.deleteById(id) > 0;
        redisCache.delete(CACHE_COURSE_ALL);
        return ok;
    }

    /**
     * 新增课程
     */
    @Override
    public boolean addCourse(Course course) {
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());
        boolean ok = courseMapper.insert(course) > 0;
        redisCache.delete(CACHE_COURSE_ALL);
        return ok;
    }

    /**
     * 根据id查询课程详情
     */
    @Override
    public Course findById(Integer id) {
        return courseMapper.selectById(id);
    }

    /**
     * 修改课程
     */
    @Override
    public boolean updateCourse(Course course) {
        course.setUpdateTime(LocalDateTime.now());
        boolean ok = courseMapper.updateById(course) > 0;
        redisCache.delete(CACHE_COURSE_ALL);
        return ok;
    }

    /**
     * 查询全部课程
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Course> findAll() {
        Object cached = redisCache.get(CACHE_COURSE_ALL);
        if (cached instanceof List<?> list) {
            return (List<Course>) list;
        }
        List<Course> courses = courseMapper.selectList(null);
        redisCache.set(CACHE_COURSE_ALL, courses, 30, java.util.concurrent.TimeUnit.MINUTES);
        return courses;
    }

    /**
     * 根据学科查询课程
     */
    @Override
    public List<Course> findBySubject(Integer subject) {
        LambdaQueryWrapper<Course> lqw = new LambdaQueryWrapper<>();
        lqw.eq(subject != null, Course::getSubject, subject);
        return courseMapper.selectList(lqw);
    }

}
