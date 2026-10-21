package cn.itcast.service.impl;

import cn.itcast.common.PageDate;
import cn.itcast.dto.DeptDto;
import cn.itcast.exception.CustomException;
import cn.itcast.mapper.DeptMapper;
import cn.itcast.pojo.Dept;
import cn.itcast.service.DeptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门业务层实现类，处理部门相关的增删改查业务
 */
@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

    @Autowired
    private DeptMapper deptMapper;

    /**
     * 分页+条件查询部门
     */
    @Override
    public PageDate<Dept> findPage(DeptDto deptDto) {
        IPage<Dept> page = new Page<>(deptDto.getPage(), deptDto.getPageSize());
        LambdaQueryWrapper<Dept> lqw = new LambdaQueryWrapper<>();
        lqw.like(deptDto.getName() != null && deptDto.getName().length() > 0, Dept::getName, deptDto.getName());
        lqw.eq(deptDto.getStatus() != null, Dept::getStatus, deptDto.getStatus());
        IPage<Dept> pageResult = deptMapper.selectPage(page, lqw);
        return new PageDate<>(pageResult.getTotal(), pageResult.getRecords());
    }

    /**
     * 根据id删除部门
     */
    @Override
    public boolean deleteById(Integer id) {
        return deptMapper.deleteById(id) > 0;
    }

    /**
     * 新增部门
     */
    @Override
    public boolean addDept(Dept dept) {
        if (dept.getName() == null || dept.getName().length() == 0) {
            throw new CustomException("部门名称不能为空!");
        }
        try {
            dept.setCreateTime(LocalDateTime.now());
            dept.setUpdateTime(LocalDateTime.now());
            return deptMapper.insert(dept) > 0;
        } catch (Exception e) {
            throw new RuntimeException("添加失败,部门名称不能重复!");
        }
    }

    /**
     * 根据id查询部门详情
     */
    @Override
    public Dept findById(Integer id) {
        return deptMapper.selectById(id);
    }

    /**
     * 修改部门
     */
    @Override
    public boolean updateDept(Dept dept) {
        dept.setUpdateTime(LocalDateTime.now());
        return deptMapper.updateById(dept) > 0;
    }

    /**
     * 查询所有启用状态的部门
     */
    @Override
    public List<Dept> findAllActive() {
        LambdaQueryWrapper<Dept> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Dept::getStatus, 1);
        return deptMapper.selectList(lqw);
    }

}
