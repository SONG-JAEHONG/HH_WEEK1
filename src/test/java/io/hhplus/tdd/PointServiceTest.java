package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class PointServiceTest {

    @Mock
    UserPointTable userPointTable;

    @Mock
    PointHistoryTable pointHistoryTable;


    @InjectMocks
    PointService pointService;

    long userId = 1L;

    @Test
    void 포인트_조회_요청시_해당_유저의_포인트가_조회된다() {
        // given
        UserPoint mockPoint = new UserPoint(userId, 5000L, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(mockPoint);

        // when
        UserPoint result = pointService.getPoint(userId);

        // then
        assertEquals(5000L, result.point());
        verify(userPointTable).selectById(userId);
    }

    @Test
    void 포인트_충전시_유저_포인트가_증가하고_히스토리가_저장된다() {
        // given
        long amount = 2000L;
        long now = System.currentTimeMillis();

        UserPoint beforeCharge = new UserPoint(userId, 1000L, now);
        UserPoint afterCharge = new UserPoint(userId, 3000L, now);

        when(userPointTable.selectById(userId)).thenReturn(beforeCharge);
        when(userPointTable.insertOrUpdate(userId, 3000L)).thenReturn(afterCharge);

        // when
        UserPoint result = pointService.charge(userId, amount);

        // then
        assertEquals(3000L, result.point());
        verify(userPointTable).selectById(userId);
        verify(userPointTable).insertOrUpdate(userId, 3000L);
        verify(pointHistoryTable).insert(userId, amount, TransactionType.CHARGE, afterCharge.updateMillis());
    }

    @Test
    void 포인트_사용시_유저_포인트가_차감되고_히스토리가_저장된다() {
        // given
        long amount = 1500L;
        long now = System.currentTimeMillis();

        UserPoint beforeUse = new UserPoint(userId, 5000L, now);
        UserPoint afterUse = new UserPoint(userId, 3500L, now);

        when(userPointTable.selectById(userId)).thenReturn(beforeUse);
        when(userPointTable.insertOrUpdate(userId, 3500L)).thenReturn(afterUse);

        // when
        UserPoint result = pointService.use(userId, amount);

        // then
        assertEquals(3500L, result.point());
        verify(userPointTable).selectById(userId);
        verify(userPointTable).insertOrUpdate(userId, 3500L);
        verify(pointHistoryTable).insert(userId, amount, TransactionType.USE, afterUse.updateMillis());
    }

    @Test
    void 포인트_사용시_잔액이_부족하면_예외를_던진다() {
        // given
        long amount = 6000L;
        UserPoint current = new UserPoint(userId, 3000L, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(current);

        // expect
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(userId, amount);
        });

        assertEquals("잔액 부족", exception.getMessage());
        verify(userPointTable).selectById(userId);
        verifyNoMoreInteractions(userPointTable);
        verifyNoInteractions(pointHistoryTable);
    }

    @Test
    void 포인트_히스토리_조회시_해당_유저의_기록을_반환한다() {
        // given
        List<PointHistory> mockHistories = List.of(
                new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis())
        );

        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(mockHistories);

        // when
        List<PointHistory> result = pointService.getHistories(userId);

        // then
        assertEquals(1, result.size());
        assertEquals(TransactionType.CHARGE, result.get(0).type());
        verify(pointHistoryTable).selectAllByUserId(userId);
    }

    @Test
    void 금요일에는_충전_보너스가_추가된다() {
        // given
        Clock fridayClock = Clock.fixed(
                LocalDate.of(2025, 7, 11).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );

        UserPointTable userPointTable = mock(UserPointTable.class);
        PointHistoryTable pointHistoryTable = mock(PointHistoryTable.class);
        PointService pointService = new PointService(userPointTable, pointHistoryTable, fridayClock);

        long userId = 1L;
        long amount = 2_000L; // 실제 충전 금액
        long expectedTotal = 3_000L; // +1,000 보너스 기대

        UserPoint before = new UserPoint(userId, 1_000L, System.currentTimeMillis());
        UserPoint after = new UserPoint(userId, 4_000L, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(before);
        when(userPointTable.insertOrUpdate(userId, 4_000L)).thenReturn(after);

        // when
        UserPoint result = pointService.charge(userId, amount);

        // then
        assertEquals(4_000L, result.point());
        verify(pointHistoryTable).insert(eq(userId), eq(3_000L), eq(TransactionType.CHARGE), anyLong());
    }

}
