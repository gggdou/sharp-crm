package cn.itcast.service;

import cn.itcast.common.PageDate;
import cn.itcast.dto.ClueDto;
import cn.itcast.pojo.Clue;
import com.baomidou.mybatisplus.spring.service.IService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 线索业务层接口，定义线索相关的业务方法
 */
public interface ClueService extends IService<Clue> {

    /**
     * 根据id查询线索详情
     */
    Clue findById(Integer id);

    /**
     * 跟进线索
     */
    @Transactional(rollbackFor = Exception.class)
    void updateFollow(Clue clue);

    /**
     * 分页查询线索
     */
    PageDate<Clue> findPage(ClueDto clueDto);

    /**
     * 分配线索
     */
    void updateAssign(Integer clueId, Integer userId);

    /**
     * 标记伪线索并跟进
     */
    @Transactional(rollbackFor = Exception.class)
    void updateFalseFollow(Integer id, ClueDto clueDto);

    /**
     * 线索转商机
     */
    @Transactional(rollbackFor = Exception.class)
    void updateToBusiness(Integer id);

    /**
     * 线索池分页查询
     */
    PageDate<Clue> findPoolPage(ClueDto clueDto);
}
