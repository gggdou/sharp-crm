package cn.itcast.mapper;

import cn.itcast.dto.UserDto;
import cn.itcast.pojo.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户数据访问层接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 分页查询用户
     */
    IPage<User> findPage(IPage<User> page, UserDto userDto);

    /**
     * 根据id查询用户详情
     */
    User findById(Integer id);

    /**
     * 根据角色标签查询用户
     */
    List<User> findByRoleLabel(String roleLabel);

    /**
     * 根据部门id查询用户
     */
    List<User> findByDept(Integer deptId);

    /**
     * 根据用户名查询用户
     */
    User findByUsername(User user);
}
