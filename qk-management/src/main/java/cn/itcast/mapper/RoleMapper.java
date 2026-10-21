package cn.itcast.mapper;

import cn.itcast.pojo.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色数据访问层接口
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
