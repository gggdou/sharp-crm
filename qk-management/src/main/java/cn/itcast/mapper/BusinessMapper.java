package cn.itcast.mapper;

import cn.itcast.dto.BusinessDto;
import cn.itcast.pojo.Business;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商机数据访问层接口
 */
@Mapper
public interface BusinessMapper extends BaseMapper<Business> {

    /**
     * 分页查询商机
     */
    IPage<Business> findPage(IPage<Business> page, BusinessDto businessDto);

    /**
     * 公海池分页查询
     */
    IPage<Business> findPoolPage(IPage<Business> page, BusinessDto businessDto);

    /**
     * 根据id查询商机详情
     */
    Business findById(Integer id);
}
