package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }


    public UserPoint getPoint(long userId) {

        return userPointTable.selectById(userId);

    }

    public List<PointHistory> getHistories(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    public UserPoint charge(long userId, long amount) {

        UserPoint current = userPointTable.selectById(userId);

        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
        if (amount > 1_000_000_000L) {
            throw new IllegalArgumentException("충전 금액은 최대 10억까지 가능합니다.");
        }


        // 없으면 0포인트 유저로 간주
        if (current == null) {
            current = new UserPoint(userId, 0L, System.currentTimeMillis());
        }

        long newAmount = current.point() + amount;
        UserPoint updated = userPointTable.insertOrUpdate(userId, newAmount);
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, updated.updateMillis());

        return updated;
    }


    public UserPoint use(long userId, long amount) {
        // 현재 포인트 조회
        UserPoint current = userPointTable.selectById(userId);

        if (amount <= 0) {
            throw new IllegalArgumentException("사용 금액은 0보다 커야 합니다.");
        }

        if (current.point() < amount) {
            throw new IllegalArgumentException("잔액 부족");
        }

        long newAmount = current.point() - amount;

        // 업데이트
        UserPoint updated = userPointTable.insertOrUpdate(userId, newAmount);

        // 히스토리 저장
        pointHistoryTable.insert(userId, amount, TransactionType.USE, updated.updateMillis());

        return updated;
    }
}
