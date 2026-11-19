package com.sharpcrm.controller;

import com.sharpcrm.common.Result;
import com.sharpcrm.pojo.User;
import com.sharpcrm.service.UserService;
import com.sharpcrm.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录控制器，处理用户登录接口
 */
@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        LoginVo loginVo = userService.login(user);
        return Result.success(loginVo);
    }

    /**
     * 用户注册（拦截器已放行）
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        return Result.success(userService.register(user));
    }

}
