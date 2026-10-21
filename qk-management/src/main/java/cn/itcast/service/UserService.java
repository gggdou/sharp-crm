package cn.itcast.service;

import cn.itcast.common.PageDate;
import cn.itcast.dto.UserDto;
import cn.itcast.pojo.User;
import cn.itcast.vo.LoginVo;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

/**
 * 用户业务层接口，定义用户相关的业务方法
 */
public interface UserService extends IService<User> {

    /**
     * 分页查询用户
     */
    PageDate<User> findPage(UserDto userDto);

    /**
     * 根据id批量删除用户
     */
    boolean deleteById(List<Integer> ids);

    /**
     * 新增用户
     */
    boolean addUser(User user);

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
     * 用户登录
     */
    LoginVo login(User user);
}
