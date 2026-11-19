package com.sharpcrm.mapper;

import com.sharpcrm.pojo.Course;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程数据访问层接口
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
