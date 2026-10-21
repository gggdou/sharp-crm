package cn.itcast.service.impl;

import cn.itcast.mapper.BusinessTrackRecordMapper;
import cn.itcast.pojo.BusinessTrackRecord;
import cn.itcast.service.BusinessTrackRecordService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 商机跟进记录业务层实现类
 */
@Service
public class BusinessTrackRecordServiceImpl extends ServiceImpl<BusinessTrackRecordMapper, BusinessTrackRecord> implements BusinessTrackRecordService {
}
