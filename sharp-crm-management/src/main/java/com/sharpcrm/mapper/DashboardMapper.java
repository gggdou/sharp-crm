package com.sharpcrm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 数据看板统计查询
 * <p>
 * 统一返回 label / cnt 两列，由业务层负责翻译成中文名称并补齐缺失月份。
 * scopeUserId 为空表示统计全部（管理员），否则只统计该用户归属的数据。
 */
@Mapper
public interface DashboardMapper {

    /**
     * 按月统计新增线索数量
     *
     * @param months      向前追溯的月份数（含本月）
     * @param scopeUserId 数据权限范围，null 表示不限
     */
    List<Map<String, Object>> countClueByMonth(@Param("months") int months,
                                               @Param("scopeUserId") Integer scopeUserId);

    /**
     * 按月统计新增商机数量
     *
     * @param months      向前追溯的月份数（含本月）
     * @param scopeUserId 数据权限范围，null 表示不限
     */
    List<Map<String, Object>> countBusinessByMonth(@Param("months") int months,
                                                   @Param("scopeUserId") Integer scopeUserId);

    /**
     * 按渠道统计线索数量
     *
     * @param scopeUserId 数据权限范围，null 表示不限
     */
    List<Map<String, Object>> countClueByChannel(@Param("scopeUserId") Integer scopeUserId);

    /**
     * 按状态统计线索数量
     *
     * @param scopeUserId 数据权限范围，null 表示不限
     */
    List<Map<String, Object>> countClueByStatus(@Param("scopeUserId") Integer scopeUserId);
}
