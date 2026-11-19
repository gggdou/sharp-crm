package com.sharpcrm.service;

import com.sharpcrm.common.PageDate;
import com.sharpcrm.dto.CustomerDto;
import com.sharpcrm.pojo.Customer;
import com.sharpcrm.vo.Customer360Vo;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * 客户服务接口
 */
public interface CustomerService extends IService<Customer> {
    PageDate<Customer> findPage(CustomerDto dto);

    /**
     * 按数据权限查询客户列表：管理员返回全部，其余角色仅返回本人名下客户
     */
    java.util.List<Customer> listWithScope();

    /**
     * 按数据权限查询客户详情，无权访问时抛业务异常
     */
    Customer findByIdWithScope(Integer id);

    /**
     * 客户 360：聚合客户信息、来源线索、来源商机与全链路跟进时间线
     */
    Customer360Vo find360(Integer id);
}
