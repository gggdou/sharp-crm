package com.sharpcrm.service;

import com.sharpcrm.vo.TodoVo;

/**
 * 待办中心服务：把分散在各业务表里的「该做的事」聚合成一个列表
 */
public interface TodoService {

    /**
     * 查询当前登录用户的待办，管理员返回全公司范围
     */
    TodoVo list();
}
