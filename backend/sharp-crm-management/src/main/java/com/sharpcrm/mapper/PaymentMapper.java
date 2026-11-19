package com.sharpcrm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sharpcrm.pojo.Payment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 回款 Mapper
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
