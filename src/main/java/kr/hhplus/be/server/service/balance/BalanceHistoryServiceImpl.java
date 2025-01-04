package kr.hhplus.be.server.service.balance;

import kr.hhplus.be.server.domain.balance.code.BalanceType;
import kr.hhplus.be.server.domain.balance.entity.BalanceHistory;
import kr.hhplus.be.server.domain.balance.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceHistoryServiceImpl implements BalanceHistoryService {

    private final BalanceHistoryRepository balanceHistoryRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BigDecimal calculate(User user) {

        List<BalanceHistory> balanceHistories = balanceHistoryRepository.findByUser(user);
        if (balanceHistories.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Map<BalanceType, BigDecimal> balanceTypeAmountMap = balanceHistories.stream()
                .collect(
                        Collectors.groupingBy(
                                BalanceHistory::getType,
                                Collectors.reducing(BigDecimal.ZERO, BalanceHistory::getAmount, BigDecimal::add)
                        )
                );

        BigDecimal availableBalance = balanceTypeAmountMap.get(BalanceType.CHARGE)
                .subtract(balanceTypeAmountMap.get(BalanceType.USE))
                .subtract(balanceTypeAmountMap.get(BalanceType.REFUND));

        // User 업데이트
        user.setBalance(availableBalance);

        return availableBalance;
    }

    @Override
    @Transactional
    public BalanceHistory use(User user, BigDecimal amount) {
        BalanceHistory balanceHistory = BalanceHistory.of()
                .user(user)
                .type(BalanceType.USE)
                .amount(amount)
                .build();

        user.setBalance(user.getBalance().subtract(amount));

        return balanceHistoryRepository.save(balanceHistory);
    }
}