package io.hhplus.tdd;

import io.hhplus.tdd.point.PointService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PointControllerExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @Test
    void 음수_금액으로_충전시_예외처리_500_응답() throws Exception {
        // given
        long userId = 1L;
        long amount = -100L;

        // when
        when(pointService.charge(userId, amount)).thenThrow(new IllegalArgumentException("충전 금액은 0보다 커야 합니다."));

        // then
        mockMvc.perform(patch("/point/1/charge")
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.message").value("충전 금액은 0보다 커야 합니다."));
    }

    @Test
    void 서비스에서_예외_던지면_컨트롤러는_500을_반환한다() throws Exception {
        // given
        long userId = 2L;
        long amount = 500L;

        // when
        when(pointService.charge(userId, amount)).thenThrow(new RuntimeException("예상치 못한 오류"));

        // then
        mockMvc.perform(patch("/point/2/charge")
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.message").value("예상치 못한 오류."));
    }

    @Test
    void 요청_파라미터_없을_때_400_응답() throws Exception {
        mockMvc.perform(patch("/point/3/charge"))
                .andExpect(status().isBadRequest());
    }
}
