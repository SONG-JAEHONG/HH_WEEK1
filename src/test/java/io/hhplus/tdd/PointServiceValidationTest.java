package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class PointServiceValidationTest {


    PointService pointService;
    UserPointTable userPointTable;
    PointHistoryTable pointHistoryTable;
    Clock clock;

    long userId = 1L;

    @BeforeEach
    void setUp() {
        userPointTable = mock(UserPointTable.class);
        pointHistoryTable = mock(PointHistoryTable.class);
        clock = Clock.system(ZoneId.systemDefault()); // 기본 시스템 시간 사용
        pointService = new PointService(userPointTable, pointHistoryTable, clock);

    }

    @Test
    void 음수_포인트_충전시_예외발생() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(userId, -100L);
        });

        assertEquals("충전 금액은 0보다 커야 합니다.", exception.getMessage());
    }

    @Test
    void 포인트_0원_충전시_예외발생() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(userId, 0L);
        });

        assertEquals("충전 금액은 0보다 커야 합니다.", exception.getMessage());
    }

    @Test
    void 포인트_10억_초과_충전시_예외발생() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(userId, 1_000_000_001L);
        });

        assertEquals("충전 금액은 최대 10억까지 가능합니다.", exception.getMessage());
    }

    @Test
    void 포인트_0원_사용시_예외발생() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(userId, 0L);
        });

        assertEquals("사용 금액은 0보다 커야 합니다.", exception.getMessage());
    }

    @Test
    void 음수_포인트_사용시_예외발생() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(userId, -500L);
        });

        assertEquals("사용 금액은 0보다 커야 합니다.", exception.getMessage());
    }

}
