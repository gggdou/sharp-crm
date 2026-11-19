package com.sharpcrm.service;

import com.sharpcrm.pojo.Business;
import com.sharpcrm.pojo.Clue;
import com.sharpcrm.service.impl.ClueServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * 线索业务层单元测试：覆盖线索转商机的状态流转与属性迁移
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClueServiceTest {

    @Mock
    private BusinessService businessService;

    @Mock
    private ClueTrackRecordService clueTrackRecordService;

    @Mock
    private com.sharpcrm.support.DataScopeHelper dataScopeHelper;

    private ClueServiceImpl newServiceWithMocks() {
        ClueServiceImpl service = spy(new ClueServiceImpl());
        ReflectionTestUtils.setField(service, "businessService", businessService);
        ReflectionTestUtils.setField(service, "clueTrackRecordService", clueTrackRecordService);
        ReflectionTestUtils.setField(service, "dataScopeHelper", dataScopeHelper);
        return service;
    }

    @Test
    void updateToBusinessCreatesBusinessAndMarksClue() {
        Clue clue = new Clue();
        clue.setId(10);
        clue.setName("张三");
        clue.setPhone("13800138000");
        clue.setStatus(3);
        clue.setUserId(5);

        ClueServiceImpl service = newServiceWithMocks();
        doReturn(clue).when(service).getById(10);
        doReturn(true).when(service).saveOrUpdate(any(Clue.class));

        service.updateToBusiness(10);

        // 线索状态置为「已转商机」：4
        assertEquals(4, clue.getStatus());

        // 捕获生成的商机，校验状态、归属清空、线索关联正确
        ArgumentCaptor<Business> captor = ArgumentCaptor.forClass(Business.class);
        verify(businessService).saveOrUpdate(captor.capture());
        Business business = captor.getValue();
        assertNull(business.getId());
        assertEquals(1, business.getStatus());
        assertNull(business.getUserId());
        assertEquals(10, business.getClueId());
        assertEquals("张三", business.getName());
    }
}
