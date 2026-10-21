package cn.itcast.service.impl;

import cn.itcast.common.PageDate;
import cn.itcast.dto.CourseDto;
import cn.itcast.mapper.CourseMapper;
import cn.itcast.pojo.Course;
import cn.itcast.service.CourseService;
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

    /**
     * 分页+条件查询课程
     */
    @Override
    public PageDate<Course> findPage(CourseDto courseDto) {
        IPage<Course> page = new Page<>(courseDto.getPage(), courseDto.getPageSize());
        LambdaQueryWrapper<Course> lqw = new LambdaQueryWrapper<>();
        lqw.like(courseDto.getName() != null && courseDto.getName().length() > 0, Course::getName, courseDto.getName());
        lqw.eq(courseDto.getSubject() != null && courseDto.getSubject().length() > 0, Course::getSubject, courseDto.getSubject());
        lqw.eq(courseDto.getTarget() != null && courseDto.getTarget().length() > 0, Course::getTarget, courseDto.getTarget());
        courseMapper.selectPage(page, lqw);
        return new PageDate<>(page.getTotal(), page.getRecords());
    }

    /**
     * 根据id删除课程
     */
    @Override
    public boolean deleteById(Integer id) {
        return courseMapper.deleteById(id) > 0;
    }

    /**
     * 新增课程
     */
    @Override
    public boolean addCourse(Course course) {
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());
        return courseMapper.insert(course) > 0;
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
        return courseMapper.updateById(course) > 0;
    }

    /**
     * 查询全部课程
     */
    @Override
    public List<Course> findAll() {
        return courseMapper.selectList(null);
    }

    /**
     * 根据学科查询课程
     */
    @Override
    public List<Course> findBySubject(String subject) {
        LambdaQueryWrapper<Course> lqw = new LambdaQueryWrapper<>();
        lqw.eq(subject != null && subject.length() > 0, Course::getSubject, subject);
        return courseMapper.selectList(lqw);
    }

}
