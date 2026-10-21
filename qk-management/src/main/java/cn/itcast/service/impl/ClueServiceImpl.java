package cn.itcast.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.itcast.common.PageDate;
import cn.itcast.dto.ClueDto;
import cn.itcast.mapper.ClueMapper;
import cn.itcast.pojo.Business;
import cn.itcast.pojo.Clue;
import cn.itcast.pojo.ClueTrackRecord;
import cn.itcast.service.BusinessService;
import cn.itcast.service.ClueService;
import cn.itcast.service.ClueTrackRecordService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 线索业务层实现类，处理线索的分配、跟进、转商机等业务
 */
@Service
public class ClueServiceImpl extends ServiceImpl<ClueMapper, Clue> implements ClueService {

    @Autowired
    private ClueTrackRecordService clueTrackRecordService;

    @Autowired
    private BusinessService businessService;

    /**
     * 根据id查询线索详情
     */
    @Override
    public Clue findById(Integer id) {
        return getBaseMapper().findById(id);
    }

    /**
     * 跟进线索：修改状态并新增跟进记录
     */
    @Override
    public void updateFollow(Clue clue) {
        clue.setStatus(3);
        saveOrUpdate(clue);

        ClueTrackRecord record = new ClueTrackRecord();
        BeanUtils.copyProperties(clue, record);
        record.setClueId(clue.getId());
        clueTrackRecordService.saveOrUpdate(record);
    }

    /**
     * 分页查询线索
     */
    @Override
    public PageDate<Clue> findPage(ClueDto clueDto) {
        IPage<Clue> page = new Page<>(clueDto.getPage(), clueDto.getPageSize());
        IPage<Clue> result = getBaseMapper().findPage(page, clueDto);
        return new PageDate<>(result.getTotal(), result.getRecords());
    }

    /**
     * 分配线索给指定用户
     */
    @Override
    public void updateAssign(Integer clueId, Integer userId) {
        Clue clue = getById(clueId);
        clue.setUserId(userId);
        clue.setStatus(2);
        saveOrUpdate(clue);
    }

    /**
     * 标记伪线索并记录伪线索原因
     */
    @Override
    public void updateFalseFollow(Integer id, ClueDto clueDto) {
        Clue clue = getById(id);
        clue.setStatus(4);
        saveOrUpdate(clue);

        ClueTrackRecord record = new ClueTrackRecord();
        BeanUtils.copyProperties(clue, record);
        record.setId(null);
        record.setClueId(clue.getId());
        record.setType(0);
        record.setFalseReason(clueDto.getReason());
        record.setRecord(clueDto.getRemark());
        clueTrackRecordService.saveOrUpdate(record);
    }

    /**
     * 线索转商机
     */
    @Override
    public void updateToBusiness(Integer id) {
        Clue clue = getById(id);
        clue.setStatus(5);
        saveOrUpdate(clue);

        Business business = new Business();
        BeanUtil.copyProperties(clue, business);
        business.setId(null);
        business.setStatus(1);
        business.setUserId(null);
        business.setClueId(clue.getId());
        businessService.saveOrUpdate(business);
    }

    /**
     * 线索池分页查询
     */
    @Override
    public PageDate<Clue> findPoolPage(ClueDto clueDto) {
        IPage<Clue> page = new Page<>(clueDto.getPage(), clueDto.getPageSize());
        IPage<Clue> result = getBaseMapper().findPoolPage(page, clueDto);
        return new PageDate<>(result.getTotal(), result.getRecords());
    }

}
