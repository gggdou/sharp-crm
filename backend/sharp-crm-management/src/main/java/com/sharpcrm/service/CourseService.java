package com.sharpcrm.service;

import com.sharpcrm.common.PageDate;
import com.sharpcrm.dto.CourseDto;
import com.sharpcrm.pojo.Course;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

/**
 * 课程业务层接口，定义课程相关的业务方法
 */
public interface CourseService extends IService<Course> {

    /**
     * 分页+条件查询课程
     */
    PageDate<Course> findPage(CourseDto courseDto);

    /**
     * 根据id删除课程
     */
    boolean deleteById(Integer id);

    /**
     * 新增课程
     */
    boolean addCourse(Course course);

    /**
     * 根据id查询课程详情
     */
    Course findById(Integer id);

    /**
     * 修改课程
     */
    boolean updateCourse(Course course);

    /**
     * 查询全部课程（不分页）
     */
    List<Course> findAll();

    /**
     * 根据学科查询课程
     */
    List<Course> findBySubject(Integer subject);
}
