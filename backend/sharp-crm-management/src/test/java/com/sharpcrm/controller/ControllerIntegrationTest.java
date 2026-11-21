package com.sharpcrm.controller;

import com.sharpcrm.common.PageDate;
import com.sharpcrm.dto.ClueDto;
import com.sharpcrm.exception.CustomException;
import com.sharpcrm.exception.GlobalException;
import com.sharpcrm.pojo.User;
import com.sharpcrm.service.ClueService;
import com.sharpcrm.service.DashboardService;
import com.sharpcrm.service.UserService;
import com.sharpcrm.vo.DashboardVo;
import com.sharpcrm.vo.LoginVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller 层集成测试：走真实 Spring MVC 调用链（请求映射 → 参数绑定 → 统一响应/异常处理），
 * Service 用 Mock 隔离，不依赖数据库，可与纯单元测试一起跑。
 */
@DisplayName("Controller 层集成测试")
class ControllerIntegrationTest {

    private MockMvc mockMvc;
    private UserService userService;
    private DashboardService dashboardService;
    private ClueService clueService;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        dashboardService = Mockito.mock(DashboardService.class);
        clueService = Mockito.mock(ClueService.class);

        LoginController loginController = new LoginController();
        ReflectionTestUtils.setField(loginController, "userService", userService);
        DashboardController dashboardController = new DashboardController();
        ReflectionTestUtils.setField(dashboardController, "dashboardService", dashboardService);
        ClueController clueController = new ClueController();
        ReflectionTestUtils.setField(clueController, "clueService", clueService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(loginController, dashboardController, clueController)
                .setControllerAdvice(new GlobalException())
                .build();
    }

    @Test
    @DisplayName("POST /login 成功返回 LoginVo，code=1 且不含 password")
    void loginSuccess() throws Exception {
        LoginVo vo = new LoginVo();
        vo.setUsername("zhangsan");
        vo.setName("张三");
        when(userService.login(any(User.class))).thenReturn(vo);

        mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content("{\"username\":\"zhangsan\",\"password\":\"zhangsan123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.username").value("zhangsan"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("GET /dashboard/stats 返回看板聚合数据")
    void dashboardStats() throws Exception {
        DashboardVo vo = new DashboardVo();
        DashboardVo.Overview overview = new DashboardVo.Overview();
        overview.setClueTotal(14L);
        vo.setOverview(overview);
        when(dashboardService.stats()).thenReturn(vo);

        mockMvc.perform(get("/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.overview.clueTotal").value(14));
    }

    @Test
    @DisplayName("GET /clues 分页参数正确绑定到 DTO")
    void cluePageBindsParams() throws Exception {
        when(clueService.findPage(any(ClueDto.class)))
                .thenReturn(new PageDate<>(0L, List.of()));

        mockMvc.perform(get("/clues").param("page", "2").param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        ArgumentCaptor<ClueDto> captor = ArgumentCaptor.forClass(ClueDto.class);
        verify(clueService).findPage(captor.capture());
        assertEquals(2, captor.getValue().getPage());
        assertEquals(5, captor.getValue().getPageSize());
    }

    @Test
    @DisplayName("业务异常经 GlobalException 统一转为 code=0")
    void businessExceptionHandled() throws Exception {
        when(dashboardService.stats()).thenThrow(new CustomException("演示异常"));

        mockMvc.perform(get("/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("演示异常"));
    }
}
