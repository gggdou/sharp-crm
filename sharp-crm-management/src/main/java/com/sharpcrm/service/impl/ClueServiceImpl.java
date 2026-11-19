package com.sharpcrm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.sharpcrm.common.PageDate;
import com.sharpcrm.dto.ClueDto;
import com.sharpcrm.mapper.ClueMapper;
import com.sharpcrm.pojo.Business;
import com.sharpcrm.pojo.Clue;
import com.sharpcrm.pojo.ClueTrackRecord;
import com.sharpcrm.vo.ClueExcel;
import com.sharpcrm.service.BusinessService;
import com.sharpcrm.service.ClueService;
import com.sharpcrm.service.ClueTrackRecordService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 线索业务层实现类，处理线索的分配、跟进、转商机等业务
 */
@Service
public class ClueServiceImpl extends ServiceImpl<ClueMapper, Clue> implements ClueService {

    @Autowired
    private ClueTrackRecordService clueTrackRecordService;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private com.sharpcrm.common.DataHolder<?> dataHolder;

    @Autowired
    private com.sharpcrm.support.DataScopeHelper dataScopeHelper;

    /**
     * 根据id查询线索详情，非管理员只能查看自己名下的线索（公海线索除外）
     */
    @Override
    public Clue findById(Integer id) {
        Clue clue = getBaseMapper().findById(id);
        if (clue != null) {
            dataScopeHelper.checkAccess(clue.getUserId(), "线索");
        }
        return clue;
    }

    /**
     * 跟进线索：修改状态并新增跟进记录
     * <p>
     * 两步写操作必须原子：状态改了但跟进记录没落库，等于这次跟进凭空消失。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFollow(Clue clue) {
        // 写操作同样校验归属，避免直接构造请求修改他人线索
        Clue exist = getById(clue.getId());
        if (exist == null) {
            throw new com.sharpcrm.exception.CustomException("线索不存在!");
        }
        dataScopeHelper.checkAccess(exist.getUserId(), "线索");

        clue.setStatus(2); // 2=跟进中
        saveOrUpdate(clue);

        ClueTrackRecord record = new ClueTrackRecord();
        BeanUtils.copyProperties(clue, record);
        record.setId(null);
        record.setClueId(clue.getId());
        record.setUserId(dataHolder.getCurrentUserId()); // 跟进人=当前登录用户
        record.setType(1); // 1=跟进记录
        clueTrackRecordService.saveOrUpdate(record);
    }

    /**
     * 分页查询线索，按当前用户角色注入数据权限范围
     */
    @Override
    public PageDate<Clue> findPage(ClueDto clueDto) {
        // 强制覆盖前端入参，避免通过手工传参绕过数据权限
        clueDto.setScopeUserId(dataScopeHelper.scopeUserId());
        IPage<Clue> page = new Page<>(clueDto.getPage(), clueDto.getPageSize());
        IPage<Clue> result = getBaseMapper().findPage(page, clueDto);
        return new PageDate<>(result.getTotal(), result.getRecords());
    }

    /**
     * 分配线索给指定用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAssign(Integer clueId, Integer userId) {
        Clue clue = getById(clueId);
        if (clue == null) {
            throw new com.sharpcrm.exception.CustomException("线索不存在!");
        }
        clue.setUserId(userId);
        clue.setStatus(2);
        saveOrUpdate(clue);
    }

    /**
     * 标记伪线索并记录伪线索原因
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFalseFollow(Integer id, ClueDto clueDto) {
        Clue clue = getById(id);
        if (clue == null) {
            throw new com.sharpcrm.exception.CustomException("线索不存在!");
        }
        dataScopeHelper.checkAccess(clue.getUserId(), "线索");
        clue.setStatus(3); // 3=伪线索
        saveOrUpdate(clue);

        ClueTrackRecord record = new ClueTrackRecord();
        BeanUtils.copyProperties(clue, record);
        record.setId(null);
        record.setClueId(clue.getId());
        record.setUserId(dataHolder.getCurrentUserId()); // 操作人=当前登录用户
        record.setType(0);
        record.setFalseReason(clueDto.getReason());
        record.setRecord(clueDto.getRemark());
        clueTrackRecordService.saveOrUpdate(record);
    }

    /**
     * 线索转商机
     * <p>
     * 线索置为已转商机 + 新建商机，任一步失败都必须整体回滚，
     * 否则会出现「线索已结束但没有对应商机」的断链数据。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateToBusiness(Integer id) {
        Clue clue = getById(id);
        if (clue == null) {
            throw new com.sharpcrm.exception.CustomException("线索不存在!");
        }
        dataScopeHelper.checkAccess(clue.getUserId(), "线索");
        clue.setStatus(4); // 4=转为商机
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

    /**
     * Excel 批量导入线索：字段拷贝 + 批量入库，跳过空行
     */
    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public int importExcel(java.util.List<ClueExcel> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (ClueExcel row : rows) {
            if (row == null || cn.hutool.core.util.StrUtil.isBlank(row.getPhone())) {
                continue;
            }
            Clue clue = new Clue();
            BeanUtil.copyProperties(row, clue);
            clue.setStatus(1); // 待分配
            clue.setUserId(null);
            save(clue);
            count++;
        }
        return count;
    }

    /**
     * 导出全部线索
     */
    @Override
    public java.util.List<ClueExcel> exportExcel() {
        // 导出同样受数据权限约束，避免普通角色导出全公司线索
        Integer scope = dataScopeHelper.scopeUserId();
        java.util.List<Clue> clues = scope == null
                ? list()
                : list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Clue>()
                        .eq(Clue::getUserId, scope));
        java.util.List<ClueExcel> rows = new java.util.ArrayList<>(clues.size());
        for (Clue clue : clues) {
            ClueExcel excel = new ClueExcel();
            BeanUtil.copyProperties(clue, excel);
            rows.add(excel);
        }
        return rows;
    }

}
