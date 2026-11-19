package com.sharpcrm.mapper;

import com.sharpcrm.dto.ClueDto;
import com.sharpcrm.pojo.Clue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 线索数据访问层接口
 */
@Mapper
public interface ClueMapper extends BaseMapper<Clue> {

    /**
     * 根据id查询线索详情
     */
    Clue findById(Integer id);

    /**
     * 分页查询线索
     */
    IPage<Clue> findPage(IPage<Clue> page, ClueDto clueDto);

    /**
     * 线索池分页查询
     */
    IPage<Clue> findPoolPage(IPage<Clue> page, ClueDto clueDto);
}
