package io.hhplus.tdd;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@SpringBootTest
@AutoConfigureMockMvc
public class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @Test
    void 포인트_충전_요청_성공() throws Exception {
        // given
        long userId = 1L;
        long amount = 1000L;
        UserPoint result = new UserPoint(userId, amount, System.currentTimeMillis());

        Mockito.when(pointService.charge(Mockito.eq(userId), Mockito.eq(amount)))
                .thenReturn(result);

        mockMvc.perform(patch("/point/1/charge")
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) userId))
                .andExpect(jsonPath("$.point").value((int) amount));
    }

    @Test
    void 포인트_사용_요청_성공() throws Exception {
        long userId = 2L;
        long initial = 5000L;
        long used = 3000L;
        UserPoint result = new UserPoint(userId, initial - used, System.currentTimeMillis());

        Mockito.when(pointService.use(Mockito.eq(userId), Mockito.eq(used)))
                .thenReturn(result);

        mockMvc.perform(patch("/point/2/use")
                        .param("amount", String.valueOf(used)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) userId))
                .andExpect(jsonPath("$.point").value((int) (initial - used)));
    }

    @Test
    void 포인트_조회_요청_성공() throws Exception {
        long userId = 3L;
        long point = 7000L;
        UserPoint result = new UserPoint(userId, point, System.currentTimeMillis());

        Mockito.when(pointService.getPoint(userId)).thenReturn(result);

        mockMvc.perform(get("/point/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) userId))
                .andExpect(jsonPath("$.point").value((int) point));
    }

    @Test
    void 포인트_히스토리_조회_성공() throws Exception {
        long userId = 4L;

        List<PointHistory> histories = List.of(
                new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 500L, TransactionType.USE, System.currentTimeMillis())
        );

        Mockito.when(pointService.getHistories(userId)).thenReturn(histories);

        mockMvc.perform(get("/point/4/histories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].amount").value(1000))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))
                .andExpect(jsonPath("$[1].amount").value(500))
                .andExpect(jsonPath("$[1].type").value("USE"));
    }

}

