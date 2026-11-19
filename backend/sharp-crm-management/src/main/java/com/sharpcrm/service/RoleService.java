package com.sharpcrm.service;

import com.sharpcrm.common.PageDate;
import com.sharpcrm.dto.RoleDto;
import com.sharpcrm.pojo.Role;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

/**
 * 角色业务层接口，定义角色相关的业务方法
 */
public interface RoleService extends IService<Role> {

    /**
     * 分页查询角色
     */
    PageDate<Role> findPage(RoleDto roleDto);

    /**
     * 新增角色
     */
    boolean addRole(Role role);

    /**
     * 根据id查询角色
     */
    Role findById(Integer id);

    /**
     * 查询全部角色
     */
    List<Role> findAll();
}
