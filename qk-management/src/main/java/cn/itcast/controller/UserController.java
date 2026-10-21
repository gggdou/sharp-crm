package cn.itcast.controller;

import cn.itcast.common.PageDate;
import cn.itcast.common.Result;
import cn.itcast.dto.UserDto;
import cn.itcast.pojo.User;
import cn.itcast.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器，处理用户的增删改查、分页与条件查询接口
 */
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户
     */
    @GetMapping
    public Result findPage(UserDto userDto) {
        log.info("分页查询用户，参数：{}", userDto);
        PageDate<User> pageDate = userService.findPage(userDto);
        return Result.success(pageDate);
    }

    /**
     * 根据id批量删除用户
     */
    @DeleteMapping("/{ids}")
    public Result deleteById(@PathVariable List<Integer> ids) {
        log.info("根据id删除用户，参数：{}", ids);
        boolean flag = userService.deleteById(ids);
        return flag ? Result.success() : Result.error("删除失败");
    }

    /**
     * 新增用户
     */
    @PostMapping
    public Result addUser(@RequestBody User user) {
        log.info("添加用户，参数：{}", user);
        boolean flag = userService.addUser(user);
        return flag ? Result.success() : Result.error("添加失败");
    }

    /**
     * 根据id查询用户详情
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        log.info("根据id查询用户，参数：{}", id);
        User user = userService.findById(id);
        return Result.success(user);
    }

    /**
     * 修改用户
     */
    @PutMapping
    public Result updateUser(@RequestBody User user) {
        log.info("修改用户，参数：{}", user);
        boolean flag = userService.saveOrUpdate(user);
        return flag ? Result.success() : Result.error("修改失败");
    }

    /**
     * 查询全部用户
     */
    @GetMapping("/list")
    public Result findAll() {
        log.info("查询所有用户");
        List<User> list = userService.list();
        return Result.success(list);
    }

    /**
     * 根据角色标签查询用户
     */
    @GetMapping("/role/{roleLabel}")
    public Result findByRoleLabel(@PathVariable String roleLabel) {
        List<User> users = userService.findByRoleLabel(roleLabel);
        return Result.success(users);
    }

    /**
     * 根据部门id查询用户
     */
    @GetMapping("/dept/{deptId}")
    public Result findByDept(@PathVariable Integer deptId) {
        log.info("根据部门查询用户，参数：{}", deptId);
        List<User> users = userService.findByDept(deptId);
        return Result.success(users);
    }

}
