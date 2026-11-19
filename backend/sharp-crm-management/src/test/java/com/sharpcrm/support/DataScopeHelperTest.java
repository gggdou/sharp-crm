package com.sharpcrm.support;

import com.sharpcrm.common.DataHolder;
import com.sharpcrm.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * 数据权限辅助类单元测试：覆盖管理员、专员、公海数据三类场景
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataScopeHelperTest {

    @Mock
    private DataHolder<Object> dataHolder;

    private DataScopeHelper helper;

    @BeforeEach
    void setUp() {
        helper = new DataScopeHelper();
        ReflectionTestUtils.setField(helper, "dataHolder", dataHolder);
    }

    @Test
    void adminHasNoScopeAndCanAccessEverything() {
        when(dataHolder.isAdmin()).thenReturn(true);

        // 管理员查询不加范围条件
        assertNull(helper.scopeUserId());
        // 任何归属人、包括归属为空的记录都放行
        assertTrue(helper.canAccess(99));
        assertTrue(helper.canAccess(null, false));
    }

    @Test
    void operatorScopedToOwnId() {
        when(dataHolder.isAdmin()).thenReturn(false);
        when(dataHolder.getCurrentUserId()).thenReturn(7);

        assertEquals(7, helper.scopeUserId());
        assertTrue(helper.canAccess(7));
        assertFalse(helper.canAccess(8));
    }

    @Test
    void unassignedRecordAllowedOnlyWhenPermitted() {
        when(dataHolder.isAdmin()).thenReturn(false);
        when(dataHolder.getCurrentUserId()).thenReturn(7);

        // 线索、商机存在公海，归属为空也应可见
        assertTrue(helper.canAccess(null, true));
        // 客户不存在公海，归属为空视为无权
        assertFalse(helper.canAccess(null, false));
    }

    @Test
    void checkAccessThrowsWhenNotOwner() {
        when(dataHolder.isAdmin()).thenReturn(false);
        when(dataHolder.getCurrentUserId()).thenReturn(7);

        CustomException ex = assertThrows(CustomException.class,
                () -> helper.checkAccess(8, "线索"));
        assertEquals("无权操作该线索", ex.getMessage());
    }

    @Test
    void checkAccessPassesForOwner() {
        when(dataHolder.isAdmin()).thenReturn(false);
        when(dataHolder.getCurrentUserId()).thenReturn(7);

        // 不抛异常即为通过
        helper.checkAccess(7, "商机");
    }
}
