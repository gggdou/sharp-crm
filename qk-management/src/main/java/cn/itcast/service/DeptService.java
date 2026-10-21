package cn.itcast.service;

import cn.itcast.common.PageDate;
import cn.itcast.dto.DeptDto;
import cn.itcast.pojo.Dept;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

/**
 * 部门业务层接口，定义部门相关的业务方法
 */
public interface DeptService extends IService<Dept> {

    /**
     * 分页+条件查询部门
     */
    PageDate<Dept> findPage(DeptDto deptDto);

    /**
     * 根据id删除部门
     */
    boolean deleteById(Integer id);

    /**
     * 新增部门
     */
    boolean addDept(Dept dept);

    /**
     * 根据id查询部门详情
     */
    Dept findById(Integer id);

    /**
     * 修改部门
     */
    boolean updateDept(Dept dept);

    /**
     * 查询所有启用状态的部门
     */
    List<Dept> findAllActive();
}
