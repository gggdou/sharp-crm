package cn.itcast.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.itcast.common.PageDate;
import cn.itcast.dto.BusinessDto;
import cn.itcast.mapper.BusinessMapper;
import cn.itcast.pojo.Business;
import cn.itcast.pojo.BusinessTrackRecord;
import cn.itcast.service.BusinessService;
import cn.itcast.service.BusinessTrackRecordService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 商机业务层实现类，处理商机的分配、跟进、踢回公海等业务
 */
@Service
public class BusinessServiceImpl extends ServiceImpl<BusinessMapper, Business> implements BusinessService {

    @Autowired
    private BusinessTrackRecordService businessTrackRecordService;

    /**
     * 商机分页查询
     */
    @Override
    public PageDate<Business> findPage(BusinessDto businessDto) {
        IPage<Business> page = new Page<>(businessDto.getPage(), businessDto.getPageSize());
        IPage<Business> results = getBaseMapper().findPage(page, businessDto);
        return new PageDate<>(results.getTotal(), results.getRecords());
    }

    /**
     * 分配商机给指定用户
     */
    @Override
    public void updateAssign(Integer businessId, Integer userId) {
        Business business = getById(businessId);
        business.setUserId(userId);
        business.setStatus(2);
        saveOrUpdate(business);
    }

    /**
     * 商机踢回公海
     */
    @Override
    public void updateBack(Integer id) {
        Business business = getById(id);
        business.setUserId(null);
        business.setStatus(1);
        saveOrUpdate(business);
    }

    /**
     * 跟进商机并新增跟进记录
     */
    @Override
    public void updateFollow(Business business) {
        business.setStatus(3);
        saveOrUpdate(business);

        BusinessTrackRecord btrs = new BusinessTrackRecord();
        BeanUtil.copyProperties(business, btrs);
        btrs.setId(null);
        btrs.setBusinessId(business.getId());
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
        return getBaseMapper().findById(id);
    }

}
