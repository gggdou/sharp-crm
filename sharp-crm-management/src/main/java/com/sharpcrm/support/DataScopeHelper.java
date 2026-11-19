package com.sharpcrm.support;

import com.sharpcrm.common.DataHolder;
import com.sharpcrm.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 数据权限辅助类
 * <p>
 * 规则：管理员可见全部数据；其他角色只能看到归属人是自己的线索、商机、客户。
 * 归属人为空的公海数据（尚未分配）对所有登录用户可见，便于抢单前预览。
 * <p>
 * 注意：范围值必须由服务端推导，不能直接信任前端入参，否则传一个空值即可绕过。
 */
@Component
public class DataScopeHelper {

    @Autowired
    private DataHolder<?> dataHolder;

    /**
     * 查询过滤用的归属人 id；返回 null 表示不限制（管理员）
     */
    public Integer scopeUserId() {
        return dataHolder.isAdmin() ? null : dataHolder.getCurrentUserId();
    }

    /**
     * 判断当前用户是否有权访问某条归属指定用户的记录（归属为空视为公海，允许访问）
     *
     * @param ownerId 记录的归属人 id
     */
    public boolean canAccess(Integer ownerId) {
        return canAccess(ownerId, true);
    }

    /**
     * 判断当前用户是否有权访问某条归属指定用户的记录
     *
     * @param ownerId         记录的归属人 id
     * @param allowUnassigned 归属为空时是否放行。线索、商机存在公海概念故放行；客户是成交后的正式数据，必须归属到人
     */
    public boolean canAccess(Integer ownerId, boolean allowUnassigned) {
        if (dataHolder.isAdmin()) {
            return true;
        }
        if (ownerId == null) {
            return allowUnassigned;
        }
        return ownerId.equals(dataHolder.getCurrentUserId());
    }

    /**
     * 校验访问权限，不通过时抛业务异常，供详情类接口调用
     */
    public void checkAccess(Integer ownerId, String dataName) {
        checkAccess(ownerId, dataName, true);
    }

    /**
     * 校验访问权限，可指定归属为空时是否放行
     */
    public void checkAccess(Integer ownerId, String dataName, boolean allowUnassigned) {
        if (!canAccess(ownerId, allowUnassigned)) {
            // 读写操作共用此校验，用「操作」而非「查看」以覆盖两种场景
            throw new CustomException("无权操作该" + dataName);
        }
    }
}
