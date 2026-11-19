package com.sharpcrm.service.impl;

import com.sharpcrm.common.PageDate;
import com.sharpcrm.dto.BusinessDto;
import com.sharpcrm.mapper.BusinessMapper;
import com.sharpcrm.pojo.Business;
import com.sharpcrm.pojo.BusinessTrackRecord;
import com.sharpcrm.pojo.Customer;
import com.sharpcrm.mapper.CustomerMapper;
import com.sharpcrm.service.BusinessService;
import com.sharpcrm.service.BusinessTrackRecordService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商机业务层实现类，处理商机的分配、跟进、踢回公海等业务
 */
@Service
public class BusinessServiceImpl extends ServiceImpl<BusinessMapper, Business> implements BusinessService {

    @Autowired
    private BusinessTrackRecordService businessTrackRecordService;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private com.sharpcrm.common.DataHolder<?> dataHolder;

    @Autowired
    private com.sharpcrm.support.DataScopeHelper dataScopeHelper;

    /**
     * 商机分页查询，按当前用户角色注入数据权限范围
     */
    @Override
    public PageDate<Business> findPage(BusinessDto businessDto) {
        // 强制覆盖前端入参，避免通过手工传参绕过数据权限
        businessDto.setScopeUserId(dataScopeHelper.scopeUserId());
        IPage<Business> page = new Page<>(businessDto.getPage(), businessDto.getPageSize());
        IPage<Business> results = getBaseMapper().findPage(page, businessDto);
        return new PageDate<>(results.getTotal(), results.getRecords());
    }

    /**
     * 分配商机给指定用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAssign(Integer businessId, Integer userId) {
        Business business = getById(businessId);
        if (business == null) {
            throw new com.sharpcrm.exception.CustomException("商机不存在!");
        }
        business.setUserId(userId);
        business.setStatus(2);
        saveOrUpdate(business);
    }

    /**
     * 商机踢回公海
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBack(Integer id) {
        Business business = getById(id);
        if (business == null) {
            throw new com.sharpcrm.exception.CustomException("商机不存在!");
        }
        business.setUserId(null);
        business.setStatus(1);
        saveOrUpdate(business);
    }

    /**
     * 跟进商机并新增跟进记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFollow(Business business) {
        // 写操作同样校验归属，避免直接构造请求修改他人商机
        Business exist = getById(business.getId());
        if (exist == null) {
            throw new com.sharpcrm.exception.CustomException("商机不存在!");
        }
        dataScopeHelper.checkAccess(exist.getUserId(), "商机");

        business.setStatus(3); // 3=跟进中
        saveOrUpdate(business);

        // 跟进要点在商机侧是 List，跟进记录侧是 varchar(50)，这里显式拼接并截断
        String keyItems = null;
        if (business.getKeyItems() != null && !business.getKeyItems().isEmpty()) {
            String joined = String.join(",", business.getKeyItems());
            keyItems = joined.length() > 50 ? joined.substring(0, 50) : joined;
        }

        BusinessTrackRecord btrs = new BusinessTrackRecord();
        btrs.setBusinessId(business.getId());
        btrs.setUserId(dataHolder.getCurrentUserId()); // 跟进人=当前登录用户
        btrs.setTrackStatus(business.getTrackStatus() == null ? 1 : business.getTrackStatus()); // 库表非空，默认「接通」
        btrs.setKeyItems(keyItems);
        btrs.setNextTime(business.getNextTime());
        btrs.setRecord(business.getRecord());
        businessTrackRecordService.saveOrUpdate(btrs);
    }

    /**
     * 公海池分页查询
     */
    @Override
    public PageDate<Business> findPoolPage(BusinessDto businessDto) {
        IPage<Business> page = new Page<>(businessDto.getPage(), businessDto.getPageSize());
        IPage<Business> results = getBaseMapper().findPoolPage(page, businessDto);
        return new PageDate<>(results.getTotal(), results.getRecords());
    }

    /**
     * 根据id查询商机详情
     */
    @Override
    public Business findById(Integer id) {
        Business business = getBaseMapper().findById(id);
        if (business != null) {
            dataScopeHelper.checkAccess(business.getUserId(), "商机");
        }
        return business;
    }

    /**
     * 商机转客户：商机状态置为5，同时生成客户记录
     */
    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Integer toCustomer(Integer id) {
        Business business = getById(id);
        if (business == null) {
            throw new com.sharpcrm.exception.CustomException("商机不存在!");
        }
        dataScopeHelper.checkAccess(business.getUserId(), "商机");

        Customer customer = new Customer();
        customer.setName(business.getName());
        customer.setPhone(business.getPhone());
        customer.setGender(business.getGender());
        customer.setAge(business.getAge());
        customer.setWechat(business.getWechat());
        customer.setQq(business.getQq());
        customer.setDegree(business.getDegree());
        customer.setJobStatus(business.getJobStatus());
        customer.setChannel(business.getChannel());
        customer.setSubject(business.getSubject());
        customer.setCourseId(business.getCourseId());
        customer.setUserId(business.getUserId());
        customer.setBusinessId(business.getId());
        customer.setClueId(business.getClueId());
        customer.setRemark(business.getRemark());
        customerMapper.insert(customer);

        business.setStatus(5); // 5=转客户
        saveOrUpdate(business);
        return customer.getId();
    }

}
