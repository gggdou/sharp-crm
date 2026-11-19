package com.sharpcrm.service.impl;

import com.sharpcrm.common.PageDate;
import com.sharpcrm.dto.RoleDto;
import com.sharpcrm.mapper.RoleMapper;
import com.sharpcrm.pojo.Role;
import com.sharpcrm.service.RoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色业务层实现类，处理角色的增删改查业务
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    /**
     * 分页查询角色
     */
    @Override
    public PageDate<Role> findPage(RoleDto roleDto) {
        IPage<Role> page = new Page<>(roleDto.getPage(), roleDto.getPageSize());
        LambdaQueryWrapper<Role> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(roleDto.getName() != null && roleDto.getName().length() > 0, Role::getName, roleDto.getName());
        lambdaQueryWrapper.eq(roleDto.getLabel() != null && roleDto.getLabel().length() > 0, Role::getLabel, roleDto.getLabel());
        roleMapper.selectPage(page, lambdaQueryWrapper);
        return new PageDate<>(page.getTotal(), page.getRecords());
    }

    /**
     * 新增角色
     */
    @Override
    public boolean addRole(Role role) {
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        return roleMapper.insert(role) > 0;
    }

    /**
     * 根据id查询角色
     */
    @Override
    public Role findById(Integer id) {
        return roleMapper.selectById(id);
    }

    /**
     * 查询全部角色
     */
    @Override
    public List<Role> findAll() {
        return roleMapper.selectList(null);
    }

}
