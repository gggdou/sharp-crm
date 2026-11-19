package com.sharpcrm.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.sharpcrm.common.JWTUtils;
import com.sharpcrm.exception.CustomException;
import com.sharpcrm.mapper.RoleMapper;
import com.sharpcrm.mapper.UserMapper;
import com.sharpcrm.pojo.User;
import com.sharpcrm.service.impl.UserServiceImpl;
import com.sharpcrm.vo.LoginVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 用户业务层单元测试：隔离数据库，覆盖登录校验与注册关键路径
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private JWTUtils jwtUtil;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl();
        // MyBatis-Plus 的 baseMapper 在父类 ServiceImpl 中，需手动注入
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
        ReflectionTestUtils.setField(userService, "roleMapper", roleMapper);
        ReflectionTestUtils.setField(userService, "jwtUtil", jwtUtil);
    }

    private User buildUser(String username, String rawPassword, Integer status) {
        User user = new User();
        user.setId(1);
        user.setUsername(username);
        user.setName("测试用户");
        user.setPassword(DigestUtil.md5Hex(rawPassword));
        user.setStatus(status);
        return user;
    }

    @Test
    void loginSuccess() {
        User input = new User();
        input.setUsername("zhangsan");
        input.setPassword("zhangsan123");
        when(userMapper.findByUsername(any(User.class))).thenReturn(buildUser("zhangsan", "zhangsan123", 1));
        when(jwtUtil.makeToken(any())).thenReturn("mock-token");

        LoginVo vo = userService.login(input);

        assertNotNull(vo);
        assertEquals("zhangsan", vo.getUsername());
        assertEquals("mock-token", vo.getToken());
    }

    @Test
    void loginWithWrongPasswordThrows() {
        User input = new User();
        input.setUsername("zhangsan");
        input.setPassword("wrong-password");
        when(userMapper.findByUsername(any(User.class))).thenReturn(buildUser("zhangsan", "zhangsan123", 1));

        assertThrows(CustomException.class, () -> userService.login(input));
    }

    @Test
    void loginWithDisabledUserThrows() {
        User input = new User();
        input.setUsername("lisi");
        input.setPassword("lisi123");
        when(userMapper.findByUsername(any(User.class))).thenReturn(buildUser("lisi", "lisi123", 0));

        assertThrows(CustomException.class, () -> userService.login(input));
    }

    @Test
    void loginWithMissingUserThrows() {
        User input = new User();
        input.setUsername("nobody");
        input.setPassword("anything");
        when(userMapper.findByUsername(any(User.class))).thenReturn(null);

        assertThrows(CustomException.class, () -> userService.login(input));
    }

    @Test
    void registerWithBlankUsernameThrows() {
        User user = new User();
        user.setUsername("");
        user.setPassword("123456");

        assertThrows(CustomException.class, () -> userService.register(user));
    }

    @Test
    void registerWithExistingUsernameThrows() {
        User user = new User();
        user.setUsername("zhangsan");
        user.setPassword("123456");
        user.setPhone("13800000001");
        user.setEmail("zhangsan@test.com");
        when(userMapper.selectOne(any())).thenReturn(buildUser("zhangsan", "123456", 1));

        assertThrows(CustomException.class, () -> userService.register(user));
    }

    @Test
    void registerWithoutPhoneThrows() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("abc123");
        user.setEmail("newuser@test.com");

        assertThrows(CustomException.class, () -> userService.register(user));
    }

    @Test
    void registerSuccessEncryptsPassword() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("abc123");
        user.setPhone("13800000002");
        user.setEmail("newuser@test.com");
        when(userMapper.selectOne(any())).thenReturn(null);
        when(roleMapper.selectOne(any())).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        boolean result = userService.register(user);

        assertTrue(result);
        assertEquals(DigestUtil.md5Hex("abc123"), user.getPassword());
        assertEquals(1, user.getStatus());
        assertEquals("newuser", user.getName()); // 未填姓名时默认取用户名
    }
}
