package cn.itcast.controller;

import cn.itcast.common.PageDate;
import cn.itcast.common.Result;
import cn.itcast.dto.RoleDto;
import cn.itcast.exception.CustomException;
import cn.itcast.pojo.Role;
import cn.itcast.pojo.User;
import cn.itcast.service.RoleService;
import cn.itcast.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色控制器，处理角色的增删改查、分页查询接口
 */
@RestController
@RequestMapping("/roles")
@Slf4j
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    /**
     * 分页查询角色
     */
    @GetMapping
    public Result findPage(RoleDto roleDto) {
        log.info("接收到的参数为:{}", roleDto);
        PageDate<Role> pageDates = roleService.findPage(roleDto);
        return Result.success(pageDates);
    }

    /**
     * 根据id删除角色（角色下存在用户时禁止删除）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        log.info("接收到的参数为:{}", id);
        // 校验该角色是否还有用户使用，有则不允许删除
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getRoleId, id);
        long count = userService.count(lambdaQueryWrapper);
        if (count > 0) {
            throw new CustomException("该角色还有用户使用,不能删除!");
        }
        boolean flag = roleService.removeById(id);
        return flag ? Result.success() : Result.error("删除失败!");
    }

    /**
     * 新增角色
     */
    @PostMapping
    public Result addRole(@RequestBody Role role) {
        log.info("接收到的参数为:{}", role);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        boolean flag = roleService.addRole(role);
        return flag ? Result.success() : Result.error("添加失败!");
    }

    /**
     * 根据id查询角色
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        log.info("接收到的参数为:{}", id);
        Role role = roleService.findById(id);
        return Result.success(role);
    }

    /**
     * 修改角色
     */
    @PutMapping
    public Result updateRole(@RequestBody Role role) {
        log.info("接收到的参数为:{}", role);
        role.setUpdateTime(LocalDateTime.now());
        boolean flag = roleService.saveOrUpdate(role);
        return flag ? Result.success() : Result.error("修改失败!");
    }

    /**
     * 查询全部角色
     */
    @GetMapping("/list")
    public Result findAll() {
        log.info("查询全部信息!");
        List<Role> roles = roleService.findAll();
        return Result.success(roles);
    }

}
