package com.sharpcrm.service;

import com.sharpcrm.common.PageDate;
import com.sharpcrm.dto.BusinessDto;
import com.sharpcrm.pojo.Business;
import com.baomidou.mybatisplus.spring.service.IService;

/**
 * 商机业务层接口，定义商机相关的业务方法
 */
public interface BusinessService extends IService<Business> {

    /**
     * 商机分页查询
     */
    PageDate<Business> findPage(BusinessDto businessDto);

    /**
     * 分配商机
     */
    void updateAssign(Integer businessId, Integer userId);

    /**
     * 商机踢回公海
     */
    void updateBack(Integer id);

    /**
     * 跟进商机
     */
    void updateFollow(Business business);

    /**
     * 公海池分页查询
     */
    PageDate<Business> findPoolPage(BusinessDto businessDto);

    /**
     * 根据id查询商机详情
     */
    Business findById(Integer id);

    /**
     * 商机转客户，返回生成的客户id
     */
    Integer toCustomer(Integer id);
}
