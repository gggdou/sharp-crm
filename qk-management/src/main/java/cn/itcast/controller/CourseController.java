package cn.itcast.controller;

import cn.itcast.common.PageDate;
import cn.itcast.common.Result;
import cn.itcast.dto.CourseDto;
import cn.itcast.pojo.Course;
import cn.itcast.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程控制器，处理课程相关的增删改查、分页与条件查询接口
 */
@RestController
@RequestMapping("/courses")
@Slf4j
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 课程分页查询
     */
    @GetMapping
    public Result findPage(CourseDto courseDto) {
        log.info("接入的数据为:{}", courseDto);
        PageDate<Course> courses = courseService.findPage(courseDto);
        return Result.success(courses);
    }

    /**
     * 根据id删除课程
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        log.info("传入的id为{}", id);
        courseService.deleteById(id);
        return Result.success();
    }

    /**
     * 新增课程
     */
    @PostMapping
    public Result addCourse(@RequestBody Course course) {
        log.info("添加课程!");
        boolean flag = courseService.addCourse(course);
        return flag ? Result.success() : Result.error("添加失败");
    }

    /**
     * 根据id查询课程详情
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        log.info("查询课程!");
        Course course = courseService.findById(id);
        return Result.success(course);
    }

    /**
     * 修改课程
     */
    @PutMapping
    public Result updateCourse(@RequestBody Course course) {
        log.info("修改课程!");
        boolean flag = courseService.updateCourse(course);
        return flag ? Result.success() : Result.error("修改失败");
    }

    /**
     * 查询全部课程列表（不分页）
     */
    @GetMapping("/list")
    public Result findAll() {
        log.info("查询所有课程!");
        List<Course> courses = courseService.findAll();
        return Result.success(courses);
    }

    /**
     * 根据学科查询课程
     */
    @GetMapping("/subject/{subject}")
    public Result findBySubject(@PathVariable String subject) {
        log.info("根据学科查询课程!");
        List<Course> courses = courseService.findBySubject(subject);
        return Result.success(courses);
    }

}
