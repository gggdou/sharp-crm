package com.sharpcrm.controller;

import com.sharpcrm.common.PageDate;
import com.sharpcrm.common.Result;
import com.sharpcrm.common.annotation.RequireRole;
import com.sharpcrm.dto.DeptDto;
import com.sharpcrm.exception.CustomException;
import com.sharpcrm.pojo.Dept;
import com.sharpcrm.pojo.User;
import com.sharpcrm.service.DeptService;
import com.sharpcrm.service.UserService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门控制器，处理部门的增删改查、分页查询接口
 */
@RestController
@RequestMapping("/depts")
@Slf4j
public class DeptController {

    @Autowired
    private DeptService deptService;

    @Autowired
    private UserService userService;

    /**
     * 分页查询部门
     */
    @GetMapping
    public Result findPage(DeptDto deptDto) {
        PageDate<Dept> pd = deptService.findPage(deptDto);
        log.info("pd:{}", pd);
        return pd == null ? Result.error() : Result.success(pd);
    }

    /**
     * 根据id删除部门（部门下存在用户时禁止删除）
     */
    @RequireRole("admin")
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        log.info("id:{}", id);
        // 校验部门下是否还存在用户，有则不允许删除
        long count = userService.count(Wrappers.<User>lambdaQuery().eq(User::getDeptId, id));
        if (count > 0) {
            throw new CustomException("该部门下有用户，不能删除");
        }
        boolean bool = deptService.deleteById(id);
        return bool ? Result.success() : Result.error();
    }

    /**
     * 新增部门
     */
    @RequireRole("admin")
    @PostMapping
    public Result addDept(@RequestBody Dept dept) {
        log.info("dept:{}", dept);
        boolean flag = deptService.addDept(dept);
        return flag ? Result.success() : Result.error();
    }

    /**
     * 根据id查询部门详情
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        log.info("id:{}", id);
        Dept dept = deptService.findById(id);
        return Result.success(dept);
    }

    /**
     * 修改部门
     */
    @RequireRole("admin")
    @PutMapping
    public Result updateDept(@RequestBody Dept dept) {
        log.info("dept:{}", dept);
        boolean flag = deptService.updateDept(dept);
        return flag ? Result.success() : Result.error();
    }

    /**
     * 查询所有启用状态的部门
     */
    @GetMapping("/list")
    public Result findAllActive() {
        List<Dept> depts = deptService.findAllActive();
        return depts == null ? Result.error() : Result.success(depts);
    }

}
