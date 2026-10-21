package cn.itcast.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.itcast.common.JWTUtils;
import cn.itcast.common.PageDate;
import cn.itcast.dto.UserDto;
import cn.itcast.exception.CustomException;
import cn.itcast.mapper.UserMapper;
import cn.itcast.pojo.User;
import cn.itcast.service.UserService;
import cn.itcast.vo.LoginVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户业务层实现类，处理用户的增删改查与登录业务
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JWTUtils jwtUtil;

    /**
     * 分页查询用户
     */
    @Override
    public PageDate<User> findPage(UserDto userDto) {
        IPage<User> page = new Page<>(userDto.getPage(), userDto.getPageSize());
        IPage<User> userIPage = getBaseMapper().findPage(page, userDto);
        return new PageDate<>(userIPage.getTotal(), userIPage.getRecords());
    }

    /**
     * 根据id批量删除用户
     */
    @Override
    public boolean deleteById(List<Integer> ids) {
        return getBaseMapper().deleteBatchIds(ids) > 0;
    }

    /**
     * 新增用户（默认密码为用户名+123的MD5值）
     */
    @Override
    public boolean addUser(User user) {
        user.setPassword(DigestUtil.md5Hex(user.getUsername() + "123"));
        return saveOrUpdate(user);
    }

    /**
     * 根据id查询用户详情
     */
    @Override
    public User findById(Integer id) {
        return getBaseMapper().findById(id);
    }

    /**
     * 根据角色标签查询用户
     */
    @Override
    public List<User> findByRoleLabel(String roleLabel) {
        return getBaseMapper().findByRoleLabel(roleLabel);
    }

    /**
     * 根据部门id查询用户
     */
    @Override
    public List<User> findByDept(Integer deptId) {
        return getBaseMapper().findByDept(deptId);
    }

    /**
     * 用户登录：校验用户名、密码、状态，成功则签发token
     */
    @Override
    public LoginVo login(User user) {
        // 根据用户名查询用户
        User loginUser = getBaseMapper().findByUsername(user);
        if (loginUser == null) {
            throw new CustomException("用户名或密码错误!");
        }

        // 将传入密码加密后与库中密码比对
        String md5Password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        if (!loginUser.getPassword().equals(md5Password)) {
            throw new CustomException("用户名或密码错误!");
        }

        // 校验用户状态，0表示禁用
        if (loginUser.getStatus() == 0) {
            throw new CustomException("用户当前状态不允许登录!");
        }

        // 生成token载荷，存入用户关键信息
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", loginUser.getId());
        claims.put("username", loginUser.getUsername());
        claims.put("name", loginUser.getName());
        claims.put("image", loginUser.getImage());

        String token = jwtUtil.makeToken(claims); // 签发token

        LoginVo loginVo = new LoginVo();
        BeanUtil.copyProperties(loginUser, loginVo);
        loginVo.setToken(token);
        return loginVo;
    }

}
