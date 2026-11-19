package com.sharpcrm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.sharpcrm.common.JWTUtils;
import com.sharpcrm.common.PageDate;
import com.sharpcrm.dto.UserDto;
import com.sharpcrm.exception.CustomException;
import com.sharpcrm.mapper.RoleMapper;
import com.sharpcrm.mapper.UserMapper;
import com.sharpcrm.pojo.Role;
import com.sharpcrm.pojo.User;
import com.sharpcrm.service.UserService;
import com.sharpcrm.vo.LoginVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

    @Autowired
    private RoleMapper roleMapper;

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
        // user 表 name/phone/email/gender/status 均为非空列，这里补齐默认值与校验
        if (StrUtil.isBlank(user.getName())) {
            user.setName(user.getUsername());
        }
        if (StrUtil.isBlank(user.getPhone())) {
            throw new CustomException("手机号不能为空!");
        }
        if (StrUtil.isBlank(user.getEmail())) {
            throw new CustomException("邮箱不能为空!");
        }
        if (user.getGender() == null) {
            user.setGender(1);
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        // 新增用户初始密码约定为「用户名 + 123」
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
        // 角色标识一并写入载荷，后续鉴权切面与数据权限无需再查库，保持接口无状态
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", loginUser.getId());
        claims.put("username", loginUser.getUsername());
        claims.put("name", loginUser.getName());
        claims.put("image", loginUser.getImage());
        claims.put("roleId", loginUser.getRoleId());
        claims.put("roleLabel", loginUser.getRoleLabel());

        String token = jwtUtil.makeToken(claims); // 签发token

        LoginVo loginVo = new LoginVo();
        BeanUtil.copyProperties(loginUser, loginVo);
        loginVo.setToken(token);
        return loginVo;
    }

    /**
     * 用户注册：校验必填项与用户名唯一性，密码加密，默认启用并赋予普通用户角色
     */
    @Override
    public boolean register(User user) {
        if (user == null || StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPassword())) {
            throw new CustomException("用户名和密码不能为空!");
        }

        // user 表中 username / name / phone / email / gender / status 均为非空列，这里补齐校验与默认值
        if (StrUtil.isBlank(user.getName())) {
            user.setName(user.getUsername());
        }
        if (StrUtil.isBlank(user.getPhone())) {
            throw new CustomException("手机号不能为空!");
        }
        if (StrUtil.isBlank(user.getEmail())) {
            throw new CustomException("邮箱不能为空!");
        }

        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUsername, user.getUsername());
        if (getBaseMapper().selectOne(query) != null) {
            throw new CustomException("用户名已存在!");
        }
        if (getBaseMapper().selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, user.getPhone())) != null) {
            throw new CustomException("手机号已被注册!");
        }
        if (getBaseMapper().selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, user.getEmail())) != null) {
            throw new CustomException("邮箱已被注册!");
        }

        // 默认普通用户角色，存在则绑定
        LambdaQueryWrapper<Role> roleQuery = new LambdaQueryWrapper<>();
        roleQuery.eq(Role::getLabel, "common_user");
        Role defaultRole = roleMapper.selectOne(roleQuery);
        if (defaultRole != null) {
            user.setRoleId(defaultRole.getId());
        }

        // 与登录校验保持一致：直接 MD5(明文密码)，不追加盐
        user.setPassword(DigestUtil.md5Hex(user.getPassword()));
        if (user.getGender() == null) {
            user.setGender(1); // 1=男，库表非空
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        return save(user);
    }

}
