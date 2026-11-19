package com.sharpcrm.service.impl;

import com.sharpcrm.mapper.BusinessTrackRecordMapper;
import com.sharpcrm.pojo.BusinessTrackRecord;
import com.sharpcrm.service.BusinessTrackRecordService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 商机跟进记录业务层实现类
 */
@Service
public class BusinessTrackRecordServiceImpl extends ServiceImpl<BusinessTrackRecordMapper, BusinessTrackRecord> implements BusinessTrackRecordService {
}
