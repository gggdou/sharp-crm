package com.sharpcrm.mapper;

import com.sharpcrm.pojo.Dept;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门数据访问层接口
 */
@Mapper
public interface DeptMapper extends BaseMapper<Dept> {
}
